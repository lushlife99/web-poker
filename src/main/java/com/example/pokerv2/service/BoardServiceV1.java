package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.dto.MessageDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.MessageType;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.enums.Position;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.repository.PlayerRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardServiceV1 {

    private static final int MAX_PLAYER = 6;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final static String TOPIC_PREFIX = "/topic/board/";

    /**
     * 24/01/04 chan
     * <p>
     * join
     * <p>
     * 1. 플레이 가능한 보드를 찾는다. 없다면 만든다.
     * 2. money를 bb로 환전한다.
     * 3. Player를 만들어서 입장시킨다.
     */

    @Transactional
    public BoardDto join(int requestBb, Principal principal) {
        System.out.println(principal.getName());
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board;

        List<Board> playableBoard = boardRepository.findFirstPlayableBoard(user.getId(), PageRequest.of(0, 1));

        if (playableBoard.size() != 0)
            board = playableBoard.get(0);

        else board = Board.builder().blind(1000).phaseStatus(PhaseStatus.WAITING).build();
        Player player = buyIn(board, user, requestBb);
        sitIn(board, player);
        board = boardRepository.save(board);
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.PLAYER_JOIN.getDetail(), new BoardDto(board)));
//        if(board.getTotalPlayer() > 1 && board.getPhaseStatus().equals(PhaseStatus.WAITING)) {
//            board = startGame(board.getId());
//        }
        return new BoardDto(board);
    }

    /**
     * 24/01/10 chan
     * <p>
     * 액션을 받을 때 고려해야 할 것
     * 1. 플레이어들의 액션이 다 끝났는가? true -> 다음 페이즈. false -> 다음 액션 순서 정해주기
     * 플레이어들의 액션이 다 끝났는지 어떻게 알 수 있을까?
     * <p>
     * 베팅을 받을 수 있는 상태의 플레이어가 없을 때 -> PlayerStatus 확인
     * <p>
     * 2. 플레이어가 어떤 액션을 했는지 확인.
     * fold, all-in -> playerStatus를 확인
     * call -> board.bet == player.callSize
     * bet -> board.bet < player.callSize
     * <p>
     * 3. 다음 액션 순서를 정해주는 방법
     * <p>
     * 먼저 현재 액션 순서인 플레이어를 기준으로 잡는다.
     * 1. 그 다음 순서의 플레이어가 액션을 할 수 있는 상태인지 체크한다. -> playerStatus
     * 2. 만약 액션을 할 수 있는 상태라면 마지막 액션을 했던 플레이어인지 체크한다.
     * <p>
     * 만약 1,2 모두 true : 현재 보드의 모든 플레이어들의 의사결정이 완료. -> 다음 페이즈 진행
     * 만약 1 -> true, 2 -> false : 그 플레이어가 다음 액션 순서이다.
     * 만약 1 -> false : 모든 플레이어가 액션을 할 수 없는 상태. 그러니까 베팅을 한 유저를 제외한 모든 유저들이 fold or all-in인 상태.
     * 모두 폴드라면 -> 베팅을 한 플레이어 승리.
     * all-in인 유저가 존재한다면 : 승자를 가려야 함. show-down 진행
     *
     * @param boardDto
     * @param principal
     */

    @Transactional
    public void action(BoardDto boardDto, Principal principal) {
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        if (!isSeatInBoard(board, principal.getName()))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        for (PlayerDto playerDto : boardDto.getPlayers()) {
            Player p = playerRepository.findById(playerDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
            p.changePlayerStatus(playerDto);
        }
        List<Player> players = board.getPlayers();
        board.changeBoardStatus(boardDto);
        int actionPos = board.getActionPos();
        int actionPlayerIdx = 0;

        boolean isAllInPlayerExist = false;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.getPosition().getPosNum() == actionPos) {
                actionPlayerIdx = i;
            }
        }

        //saveAction(boardDto); // actionService -> migration

        for (int i = 1; i <= board.getTotalPlayer(); i++) {

            Player nextActionCandidate = players.get((actionPlayerIdx + i) % board.getTotalPlayer());
            if (nextActionCandidate.getStatus().equals(PlayerStatus.PLAY)) {
                board.setLastActionTime(LocalDateTime.now());
                if (nextActionCandidate.getPosition().getPosNum() != board.getBettingPos()) {
                    board.setActionPos(nextActionCandidate.getPosition().getPosNum());
                    simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.NEXT_ACTION.getDetail(), new BoardDto(board)));
                    return;
                } else {
                    board = beforeNextPhase(board);
                    nextPhase(board);
                    simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.NEXT_PHASE_START.getDetail(), new BoardDto(board)));
                    return;
                }
            } else if (nextActionCandidate.getStatus().equals(PlayerStatus.ALL_IN)) {
                isAllInPlayerExist = true;
            }
        }

        /**
         * 게임이 종료된 시점.
         * if 올인한 플레이어 존재 -> 쇼다운 로직 짜기.
         * else 모두 폴드 -> 베팅 한 플레이어 승리.
         */

        if (isAllInPlayerExist) {
            GameResultDto gameResultDto = showDown(board);
            takePot(board);
            simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + boardDto.getId(), new MessageDto(MessageType.GAME_RESULT.getDetail(), gameResultDto));
        } else {
            takePot(board);
        }
    }

    /**
     * 쇼다운
     *
     * 1. 오버벳 반환
     * 2. 승자 가리기
     * 3. 팟 분배하기 (사이드 팟 생각)
     */
    public GameResultDto showDown(Board board) {

        long[][] valueAndJokBoList = HandCalculator.calculateValue(board);


        return null;
    }

    public void takePot(Board board) {

    }

    @Transactional
    public void nextPhase(Board board) {
        if (board.getPhaseStatus() == PhaseStatus.PRE_FLOP)
            board.setPhaseStatus(PhaseStatus.FLOP);
        else if (board.getPhaseStatus() == PhaseStatus.FLOP)
            board.setPhaseStatus(PhaseStatus.TURN);
        else if (board.getPhaseStatus() == PhaseStatus.TURN)
            board.setPhaseStatus(PhaseStatus.RIVER);
        else if (board.getPhaseStatus() == PhaseStatus.RIVER) {
            showDown(board);
            takePot(board);
        }

    }


    /**
     * 다음 페이즈에 진입하기 전에 보드 상태를 초기화한다.
     *
     * @return
     */
    private Board beforeNextPhase(Board board) {
        List<Player> players = board.getPlayers();

        for (Player player : players) {
            board.setPot(board.getPot() + player.getPhaseCallSize());
            player.setPhaseCallSize(0);
        }
        int btnPlayerIdx = getBtnPlayerIdx(board);
        board.setActionPos(players.get((btnPlayerIdx + 1) % players.size()).getPosition().getPosNum());
        return boardRepository.save(board);
    }

    private void saveAction(BoardDto boardDto) {
        List<PlayerDto> players = boardDto.getPlayers();
        PlayerDto actPlayer = players.get(boardDto.getActionPos());
        if (actPlayer.getStatus() == PlayerStatus.FOLD.ordinal()) {
            //fold

        } else if (actPlayer.getStatus() == PlayerStatus.ALL_IN.ordinal()) {
            //all-in
        } else if (actPlayer.getStatus() == PlayerStatus.PLAY.ordinal() && boardDto.getActionPos() == boardDto.getBettingPos()) {
            //bet
        } else if (actPlayer.getStatus() == PlayerStatus.PLAY.ordinal() && actPlayer.getPhaseCallSize() == boardDto.getBettingSize()) {
            //call
        } else throw new CustomException(ErrorCode.BAD_REQUEST);
    }

    public void test() {
        throw new CustomException(ErrorCode.BAD_REQUEST);
    }

    @Transactional()
    public Player buyIn(Board board, User user, int bb) {

        int money = user.getMoney();
        int blind = board.getBlind();
        if (money < blind * bb)
            throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

        user.setMoney(user.getMoney() - blind * bb);
        Player player = Player.builder().money(blind * bb).board(board).status(PlayerStatus.FOLD).user(user).build();

        return playerRepository.save(player);
    }

    /**
     * 첫번째 베팅 순서를 어떻게 정할까?
     * 1. 프리플랍 : BB 다음 포지션
     * 2. 프리플랍 이후 : btn 다음 포지션
     * <p>
     * 주의 해야 할 것.
     * 각 포지션이 모두 차있다고 생각하지 말 것.
     * 예를들어서 btn 포지션에 무조건 사람이 앉아있는 것이 아님.
     *
     * @param boardId
     * @return
     */

    @Transactional
    public Board startGame(Long boardId) {

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        setBtnExistPlayer(board);
        setFirstActionPos(board);
        takeAnte(board);
        board.setPhaseStatus(PhaseStatus.PRE_FLOP);
        dealCard(board);
        for (Player player : board.getPlayers()) {
            player.setStatus(PlayerStatus.PLAY);
        }
        board.setLastActionTime(LocalDateTime.now());
        boardRepository.save(board);
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.GAME_START.getDetail(), new BoardDto(board)));
        return board;
    }


    public void setBtnExistPlayer(Board board) {
        List<Player> players = board.getPlayers();

        int nextBtn = (board.getBtn() + 1) % MAX_PLAYER;

        while (true) {
            boolean isExist = false;
            for (int i = 0; i < board.getTotalPlayer(); i++) {
                Player player = players.get(i);
                if (player.getPosition().getPosNum() == nextBtn) {
                    board.setBtn(nextBtn);
                    isExist = true;
                }
            }
            if (isExist) {
                break;
            }
            nextBtn = (nextBtn + 1) % MAX_PLAYER;
        }
    }

    public void takeAnte(Board board) {
        List<Player> players = board.getPlayers();
        int btnPlayerIdx = getBtnPlayerIdx(board);
        if (btnPlayerIdx != -1) {
            if (board.getTotalPlayer() != 2) {
                Player player = players.get((btnPlayerIdx + 1) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney((int) (player.getMoney() - board.getBlind() * 0.5));
                    player.setPhaseCallSize((int) (board.getBlind() * 0.5));
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

                player = players.get((btnPlayerIdx + 2) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney(player.getMoney() - board.getBlind());
                    player.setPhaseCallSize(board.getBlind());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

            } else {
                Player player = players.get(btnPlayerIdx);
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney((int) (player.getMoney() - board.getBlind() * 0.5));
                    player.setPhaseCallSize((int) (board.getBlind() * 0.5));
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

                player = players.get((btnPlayerIdx + 1) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney(player.getMoney() - board.getBlind());
                    player.setPhaseCallSize(board.getBlind());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);
            }
        } else throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);

    }

    private int getBtnPlayerIdx(Board board) {
        List<Player> players = board.getPlayers();

        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            if (player.getPosition().getPosNum() == board.getBtn())
                return i;
        }

        return -1;
    }

    private void setFirstActionPos(Board board) {
        List<Player> players = board.getPlayers();
        int btnPlayerIdx = getBtnPlayerIdx(board);
        int actPos = players.get( (btnPlayerIdx + 1) % players.size()).getPosition().getPosNum();
        board.setActionPos(actPos);
        board.setBettingPos(actPos);

    }

    public BoardDto get(Long boardId, Principal principal) {
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        boolean isAuthenticated = false;
        for (Player p : board.getPlayers()) {
            if (p.getUser().equals(user)) {
                isAuthenticated = true;
                break;
            }

        }

        if (isAuthenticated)
            return new BoardDto(board);

        return null;
    }


    @Transactional
    public void sitIn(Board board, Player joinPlayer) {
        List<Player> players = board.getPlayers();
        boolean[] isExistSeat = new boolean[MAX_PLAYER];
        Random random = new Random();
        int pos = random.nextInt(MAX_PLAYER);

        for (Player player : players) {
            isExistSeat[player.getPosition().ordinal()] = true;
        }

        for (int i = 0; i < MAX_PLAYER; i++) {
            if (isExistSeat[pos]) {
                pos = (pos + 1) % MAX_PLAYER;
            } else {
                joinPlayer.setPosition(Position.getPositionByNumber(pos));
                players.add(joinPlayer);
                board.setTotalPlayer(board.getTotalPlayer() + 1);
                break;
            }
        }
    }

    @Transactional
    public BoardDto sitOut(BoardDto boardDto, Principal principal) {
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        Player exitPlayer;
        for (Player player : players) {
            if (player.getUser().equals(user)) {
                exitPlayer = playerRepository.findById(player.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
                user.setMoney(user.getMoney() + exitPlayer.getMoney());
                if (board.getPhaseStatus() != PhaseStatus.WAITING && player.getPhaseCallSize() != 0) {
                    board.setPot(board.getPot() + player.getPhaseCallSize());
                }

                players.remove(exitPlayer);
                board.setPlayers(players);
                board.setTotalPlayer(board.getTotalPlayer() - 1);
                playerRepository.delete(exitPlayer);
                simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.PLAYER_EXIT.getDetail(), new BoardDto(board)));
                break;
            }
        }
        return new BoardDto(board);
    }

    private boolean isSeatInBoard(Board board, String userId) {
        for (Player player : board.getPlayers()) {
            if (player.getUser().getUserId().equals(userId))
                return true;
        }
        return false;
    }

    private void dealCard(Board board) {
        Set<Integer> cards = new HashSet<>();
        Random random = new Random();

        int cardCount = board.getTotalPlayer() * 2 + 5;

        while (cards.size() < cardCount) {
            int randomNumber = random.nextInt(52);
            cards.add(randomNumber);
        }

        List<Integer> cardList = new ArrayList<>(cards);

        for (int i = 0; i < 5; i++) {
            int communityCard = cardList.get(i);
            setCommunityCard(board, i + 1, communityCard);
        }

        int playerCardIndex = 5;
        List<Player> players = board.getPlayers();

        for (Player player : players) {
            int playerCard = cardList.get(playerCardIndex);
            int playerCard2 = cardList.get(playerCardIndex + 1);
            player.setCard1(playerCard);
            player.setCard2(playerCard2);
            playerCardIndex += 2;
        }
    }

    private void setCommunityCard(Board board, int order, int card) {
        switch (order) {
            case 1:
                board.setCommunityCard1(card);
                break;
            case 2:
                board.setCommunityCard2(card);
                break;
            case 3:
                board.setCommunityCard3(card);
                break;
            case 4:
                board.setCommunityCard4(card);
                break;
            case 5:
                board.setCommunityCard5(card);
                break;
            default:
                break;
        }
    }
}


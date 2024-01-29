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
import com.example.pokerv2.utils.HandCalculatorUtils;
import com.example.pokerv2.utils.PotDistributorUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardServiceV1 {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final EntityManager entityManager;
    private static final int MAX_PLAYER = 6;
    private final static String TOPIC_PREFIX = "/topic/board/";
    private final static int RESULT_ANIMATION_DELAY = 5;
    private final static int ACTION_TIME = 10;

    /**
     * join - 24/01/04 chan
     * 1. 플레이 가능한 보드를 찾는다.
     * 2. money를 bb로 환전한다.
     * 3. Player 입장.
     */

    @Transactional
    public BoardDto join(int requestBb, Principal principal) {
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


    @Transactional
    public void endGameTest(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        board.setPhaseStatus(PhaseStatus.SHOWDOWN);
        endGame(board);
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
     * @return 다음 액션이 존재하는지 boolean 리턴.
     */


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean action(BoardDto boardDto) {

        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        int nextActionPos = getNextActionPos(board);

        int gameSeq = boardDto.getGameSeq();
        if (nextActionPos == -1) {
            if (isGameEnd(board)) {
                endGame(board);
                return false;
            } else {
                nextPhase(board);
                Player nextActionPlayer = players.get(getPlayerIdxByPos(board, board.getActionPos()));
                if (board.getPhaseStatus() == PhaseStatus.SHOWDOWN) {
                    endGame(board);
                    return false;
                } else if(nextActionPlayer.getStatus().getStatusNum() <= PlayerStatus.FOLD.getStatusNum()){
                    //boardDto = new BoardDto(board);
                } else {
                    simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.NEXT_PHASE_START.getDetail(), new BoardDto(board)));
                }
                return true;
            }
        } else {
            int nextActionIdx = getPlayerIdxByPos(board, nextActionPos);
            Player nextActionPlayer = players.get(nextActionIdx);

            board.setLastActionTime(LocalDateTime.now());
            board.setActionPos(nextActionPlayer.getPosition().getPosNum());
            simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.NEXT_ACTION.getDetail(), new BoardDto(board)));

            if (nextActionPlayer.getStatus() == PlayerStatus.DISCONNECT_PLAYED) {
                boardDto = new BoardDto(board);
                return true;
            } else {
                return true;
            }
        }
    }

    public static void waitDisconnectPlayer() {
        try {
            Thread.sleep(ACTION_TIME * 1000);
        } catch (InterruptedException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void setPlayerDisconnectFold(BoardDto boardDto) {

        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Player player = board.getPlayers().get(getPlayerIdxByPos(board, board.getActionPos()));
        player.setStatus(PlayerStatus.DISCONNECT_FOLD);
    }

    @Transactional
    public boolean isActionPlayerConnect(BoardDto boardDto) {
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        int actionPlayerIdx = getPlayerIdxByPos(board, board.getActionPos());
        Player player = players.get(actionPlayerIdx);

        if(player.getStatus().getStatusNum() >= PlayerStatus.FOLD.getStatusNum())
            return true;

        return false;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public Board getRecentBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Board saveBoardChanges(BoardDto boardDto, String option, String userId) {

        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        if (!isSeatInBoard(board, userId))
            throw new CustomException(ErrorCode.BAD_REQUEST);

        for (PlayerDto playerDto : boardDto.getPlayers()) {
            Player p = playerRepository.findById(playerDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
            if (p.getUser().getUserId().equals(userId)) {
                p.changePlayerStatus(playerDto);
                if (p.getStatus().getStatusNum() < PlayerStatus.FOLD.getStatusNum()) {
                    if (option.equals("fold")) {
                        p.setStatus(PlayerStatus.DISCONNECT_FOLD);
                    } else if (option.equals("allin")) {
                        p.setStatus(PlayerStatus.DISCONNECT_ALL_IN);
                    } else {
                        p.setStatus(PlayerStatus.DISCONNECT_PLAYED);
                    }
                } else {
                    if (option.equals("fold")) {
                        p.setStatus(PlayerStatus.FOLD);
                    } else if (option.equals("allin")) {
                        p.setStatus(PlayerStatus.ALL_IN);
                    } else {
                        p.setStatus(PlayerStatus.PLAY);
                    }
                }
            }
        }
        board.changeBoardStatus(boardDto);
        return board;
    }

    private boolean isActionable(Board board) {

        List<Player> players = board.getPlayers();
        int actionableCount = 0;

        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            if (player.getStatus() == PlayerStatus.PLAY || player.getStatus() == PlayerStatus.DISCONNECT_PLAYED) {
                actionableCount++;
            }
        }

        if (actionableCount < 2) {
            return false;
        }
        return true;
    }

    public int getNextActionPos(Board board) {
        List<Player> players = board.getPlayers();
        int currentActPlayerIdx = getPlayerIdxByPos(board, board.getActionPos());
        for (int i = 1; i < players.size(); i++) {
            Player player = players.get((currentActPlayerIdx + i) % players.size());
            PlayerStatus status = player.getStatus();

            if (player.getPosition().getPosNum() == board.getBettingPos()) {
                return -1;
            }

            if (status == PlayerStatus.PLAY || status == PlayerStatus.DISCONNECT_PLAYED) {
                return player.getPosition().getPosNum();
            }
        }

        return -1;
    }

    /**
     * GameEnd를 만족하는 상황인지 체크한다.
     *
     * @param board
     * @return
     */
    private boolean isGameEnd(Board board) {
        for (Player player : board.getPlayers()) {
            int foldCount = 0;
            if (player.getStatus() == PlayerStatus.FOLD || player.getStatus() == PlayerStatus.DISCONNECT_FOLD) {
                foldCount++;
            }

            if (foldCount == board.getTotalPlayer() - 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * endGame
     * <p>
     * 게임을 종료시킨다.
     * 1. if 한명 빼고 다 폴드 -> 남은 플레이어 승리
     * 2. fold하지 않은 플레이어가 두명 이상 -> 쇼다운
     *
     * @param board
     */
    public void endGame(Board board) {

        initPhase(board);
        List<Player> players = board.getPlayers();

        if (isGameEnd(board)) {
            winOnePlayer(board);
        } else {
            showDown(board);
        }

        initBoard(board);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }

        if (board.getTotalPlayer() >= 2) {
            startGame(board.getId());
        }
        boardRepository.save(board);
    }

    public void initBoard(Board board) {
        board.setPot(0);
        board.setBettingSize(0);
        board.setBettingPos(0);
        board.setActionPos(0);
        board.setPhaseStatus(PhaseStatus.WAITING);
        board.setCommunityCard1(0);
        board.setCommunityCard2(0);
        board.setCommunityCard3(0);
        board.setCommunityCard4(0);
        board.setCommunityCard5(0);
        List<Player> players = board.getPlayers();
        List<Player> disConnectedPlayers = new ArrayList<>();
        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            if (player.getStatus().getStatusNum() < PlayerStatus.FOLD.getStatusNum()) {
                User user = player.getUser();
                user.setMoney(user.getMoney() + player.getMoney());
                disConnectedPlayers.add(player);

            } else {
                player.setCard1(0);
                player.setCard2(0);
                player.setPhaseCallSize(0);
                player.setStatus(PlayerStatus.FOLD);
            }
        }
        players.removeAll(disConnectedPlayers);
        board.setTotalPlayer(players.size());
        List<Integer> totalCallSizeList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0));
        board.setTotalCallSize(totalCallSizeList);
        playerRepository.deleteAll(disConnectedPlayers);
        boardRepository.save(board);
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.INIT_BOARD.getDetail(), new BoardDto(board)));
    }

    @Transactional
    public void refundOverBet(Board board) {
        int bettingPlayerIdx = getPlayerIdxByPos(board, board.getBettingPos());
        int bettingSize = board.getBettingSize();
        int maxCallSize = -1;
        List<Player> players = board.getPlayers();


        for (int i = bettingPlayerIdx + 1; i < bettingPlayerIdx + 1 + board.getTotalPlayer(); i++) {
            Player player = players.get(i % board.getTotalPlayer());
            if (player.getPhaseCallSize() == bettingSize)
                return;
            else if (maxCallSize < player.getPhaseCallSize()) {
                maxCallSize = player.getPhaseCallSize();
            }
        }

        Player overBetPlayer = players.get(bettingPlayerIdx);
        overBetPlayer.setMoney(overBetPlayer.getMoney() + bettingSize - maxCallSize);
        overBetPlayer.setPhaseCallSize(maxCallSize);
    }

    public BoardDto winOnePlayer(Board board) {
        List<Player> players = board.getPlayers();
        BoardDto boardDto = new BoardDto(board);
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            PlayerDto playerDto = boardDto.getPlayers().get(i);
            if (player.getStatus() == PlayerStatus.PLAY || player.getStatus() == PlayerStatus.ALL_IN) {
                playerDto.setGameResult(
                        GameResultDto.builder().isWinner(true)
                                .earnedMoney(board.getPot())
                                .build()
                );
                player.setMoney(player.getMoney() + board.getPot());

            } else {
                playerDto.setGameResult(
                        GameResultDto.builder()
                                .isWinner(false).build());
            }
        }

        boardRepository.save(board);

        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.GAME_END.getDetail(), boardDto));

        try {
            Thread.sleep(RESULT_ANIMATION_DELAY * 1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }


        return boardDto;
    }

    /**
     * 쇼다운
     * <p>
     * 1. 오버벳 반환
     * 2. 승자 가리기
     * 3. 팟 분배하기 (사이드 팟 생각)
     */
    public BoardDto showDown(Board board) {

        refundOverBet(board);
        BoardDto boardDto = determineWinner(board);
        PotDistributorUtils.distribute(boardDto);
        int winnerCount = 0;
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.SHOW_DOWN.getDetail(), boardDto));
        for (PlayerDto player : boardDto.getPlayers()) {
            GameResultDto gameResult = player.getGameResult();
            if (gameResult.isWinner()) {
                winnerCount++;
            }
        }

        try {
            Thread.sleep(RESULT_ANIMATION_DELAY * winnerCount * 1000);
        } catch (InterruptedException e) {
            e.getStackTrace();
        }
        return boardDto;
    }

    private static BoardDto determineWinner(Board board) {
        List<Player> players = board.getPlayers();
        List<Integer> communityCards = new ArrayList<>(List.of(board.getCommunityCard1(), board.getCommunityCard2(), board.getCommunityCard3(), board.getCommunityCard4(), board.getCommunityCard5()));
        GameResultDto gameResultDto;
        BoardDto boardDto = new BoardDto(board);

        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);

            if (player.getStatus() == PlayerStatus.FOLD) {
                gameResultDto = GameResultDto.builder().isWinner(false).build();
            } else {
                ArrayList<Integer> cardPool = new ArrayList<>(List.copyOf(communityCards));
                cardPool.add(player.getCard1());
                cardPool.add(player.getCard2());
                gameResultDto = HandCalculatorUtils.calculateValue(cardPool);
            }
            boardDto.getPlayers().get(i).setGameResult(gameResultDto);
        }
        return boardDto;
    }

    /**
     * initBet
     * <p>
     * 페이즈가 끝났을 때 각 플레이어들의 베팅을 팟에 모음.
     *
     * @param board
     */
    public void initPhase(Board board) {
        List<Player> players = board.getPlayers();
        List<Integer> totalCallSize = board.getTotalCallSize();
        for(int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            totalCallSize.set(i, totalCallSize.get(i) + player.getPhaseCallSize());
            board.setPot(board.getPot() + player.getPhaseCallSize());
            player.setPhaseCallSize(0);
        }

        board.setBettingSize(0);

    }

    /**
     * 다음 페이즈를 시작한다.
     *
     * @param board
     */
    @Transactional
    public void nextPhase(Board board) {
        beforeNextPhase(board);

        if (board.getPhaseStatus() == PhaseStatus.PRE_FLOP) {
            board.setPhaseStatus(PhaseStatus.FLOP);
        } else if (board.getPhaseStatus() == PhaseStatus.FLOP) {
            board.setPhaseStatus(PhaseStatus.TURN);
        } else if (board.getPhaseStatus() == PhaseStatus.TURN) {
            board.setPhaseStatus(PhaseStatus.RIVER);
        } else if (board.getPhaseStatus() == PhaseStatus.RIVER) {
            board.setPhaseStatus(PhaseStatus.SHOWDOWN);
        }
    }


    /**
     * 다음 페이즈에 진입하기 전에 보드 상태를 초기화한다.
     *
     * @return
     */
    private Board beforeNextPhase(Board board) {
        List<Player> players = board.getPlayers();
        int betPos = players.get((getPlayerIdxByPos(board, board.getBtn()) + 1) % players.size()).getPosition().getPosNum();
        initPhase(board);
        board.setActionPos(betPos);
        board.setBettingPos(betPos);
        board.setLastActionTime(LocalDateTime.now());
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
        takeAnte(board);
        setFirstActionPos(board);
        board.setPhaseStatus(PhaseStatus.PRE_FLOP);
        dealCard(board);
        for (Player player : board.getPlayers()) {
            player.setStatus(PlayerStatus.PLAY);
        }
        board.setGameSeq(board.getGameSeq() + 1);
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
        int btnPlayerIdx = getPlayerIdxByPos(board, board.getBtn());
        if (btnPlayerIdx != -1) {
            if (board.getTotalPlayer() != 2) {
                Player player = players.get((btnPlayerIdx + 1) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney((int) (player.getMoney() - board.getBlind() * 0.5));
                    player.setPhaseCallSize((int) (board.getBlind() * 0.5));
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

                player = players.get((btnPlayerIdx + 2) % players.size());
                Player firstActionPlayer = players.get((btnPlayerIdx + 3) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney(player.getMoney() - board.getBlind());
                    player.setPhaseCallSize(board.getBlind());
                    board.setBettingPos(firstActionPlayer.getPosition().getPosNum());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

            } else {
                Player player = players.get(btnPlayerIdx);
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney((int) (player.getMoney() - board.getBlind() * 0.5));
                    player.setPhaseCallSize((int) (board.getBlind() * 0.5));
                    board.setBettingPos(player.getPosition().getPosNum());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

                player = players.get((btnPlayerIdx + 1) % players.size());
                if (player.getMoney() > board.getBlind()) {
                    player.setMoney(player.getMoney() - board.getBlind());
                    player.setPhaseCallSize(board.getBlind());
                } else throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);
            }
            board.setBettingSize(board.getBlind());
            board.setLastActionTime(LocalDateTime.now());
        } else throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);

    }

    private int getPlayerIdxByPos(Board board, int posNum) {
        List<Player> players = board.getPlayers();

        for (int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            if (player.getPosition().getPosNum() == posNum)
                return i;
        }

        return -1;
    }

    private void setFirstActionPos(Board board) {

        if (board.getTotalPlayer() == 2) {
            board.setActionPos(board.getBtn());
        } else {
            board.setActionPos(board.getBettingPos());
        }
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

    @Transactional(readOnly = true)
    public List<BoardDto> getContext(Principal principal) {
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> playerList = user.getPlayerList();
        List<BoardDto> context = new ArrayList<>();
        for (Player player : playerList) {
            context.add(new BoardDto(player.getBoard()));
        }
        return context;
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
    public void dropDisconnectedPlayer(Board board, Player player) {


    }

    public void setBtnPrevPlayer(Board board) {

        int btnPlayerIdx = getPlayerIdxByPos(board, board.getBtn());
        int prevPlayerIdx;
        if (btnPlayerIdx - 1 < 0) {
            prevPlayerIdx = board.getPlayers().size() - 1;
        } else {
            prevPlayerIdx = btnPlayerIdx - 1;
        }
        Position prevPlayerPos = board.getPlayers().get(prevPlayerIdx).getPosition();
        board.setBtn(prevPlayerPos.getPosNum());
    }

    public void setBettingPosNextPlayer(Board board) {

        int betPlayerIdx = getPlayerIdxByPos(board, board.getBettingPos());
        int nextPlayerIdx;
        if (betPlayerIdx + 1 <= board.getTotalPlayer()) {
            nextPlayerIdx = 0;
        } else {
            nextPlayerIdx = betPlayerIdx + 1;
        }

        Position updateBetPos = board.getPlayers().get(nextPlayerIdx).getPosition();
        board.setBettingPos(updateBetPos.getPosNum());
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void sitOut(BoardDto boardDto, String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        int actionablePlayerCount = 0;

        Player exitPlayer;
        for (Player player : players) {
            if (player.getUser().equals(user)) {
                exitPlayer = playerRepository.findById(player.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

                if (exitPlayer.getPosition().getPosNum() == board.getBtn()) {
                    setBtnPrevPlayer(board);
                }

                if (board.getBettingPos() == exitPlayer.getPosition().getPosNum()) {
                    setBettingPosNextPlayer(board);
                }

                user.setMoney(user.getMoney() + exitPlayer.getMoney());
                if (board.getPhaseStatus() != PhaseStatus.WAITING && player.getPhaseCallSize() != 0) {
                    board.setPot(board.getPot() + player.getPhaseCallSize());
                }

                players.remove(exitPlayer);
                board.setPlayers(players);
                board.setTotalPlayer(board.getTotalPlayer() - 1);
                playerRepository.delete(exitPlayer);
                boardRepository.save(board);
                break;
            }
        }

        BoardDto boardDto1 = new BoardDto(board);
        simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + board.getId(), new MessageDto(MessageType.PLAYER_EXIT.getDetail(), boardDto1));

        for (Player player : players) {
            if (player.getStatus() == PlayerStatus.PLAY || player.getStatus() == PlayerStatus.DISCONNECT_PLAYED)
                actionablePlayerCount++;
        }

        if (actionablePlayerCount == 1) {
            endGame(board);
        }
    }
    @Transactional
    public boolean isSeatInBoard(Board board, String userId) {
        List<Player> players = board.getPlayers();
        for (Player player : players) {
            if (player.getUser().getUserId().equals(userId))
                return true;
        }
        return false;
    }

    private void dealCard(Board board) {
        Set<Integer> cards = new HashSet<>();
        Random random = new Random();
        int playerCardIdxPrefix = 5;
        int cardSize = board.getTotalPlayer() * 2 + 5;

        while (cards.size() < cardSize) {
            int randomNumber = random.nextInt(52);
            cards.add(randomNumber);
        }

        List<Integer> cardList = new ArrayList<>(cards);
        board.setCommunityCard1(cardList.get(0));
        board.setCommunityCard2(cardList.get(1));
        board.setCommunityCard3(cardList.get(2));
        board.setCommunityCard4(cardList.get(3));
        board.setCommunityCard5(cardList.get(4));

        List<Player> players = board.getPlayers();

        for (Player player : players) {
            int playerCard = cardList.get(playerCardIdxPrefix);
            int playerCard2 = cardList.get(playerCardIdxPrefix + 1);
            player.setCard1(playerCard);
            player.setCard2(playerCard2);
            playerCardIdxPrefix += 2;
        }
    }
}


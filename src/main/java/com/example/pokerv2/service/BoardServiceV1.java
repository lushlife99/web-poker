package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.enums.Position;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.BoardRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardServiceV1 {

    private static final int MAX_PLAYER = 6;
    private final BoardRepository boardRepository;
    private final SessionManager sessionManager;



    /**
     * 24/01/04 chan
     *
     * join
     *
     * 1. 플레이 가능한 보드를 찾는다. 없다면 만든다.
     * 2. money를 bb로 환전한다.
     * 3. Player를 만들어서 입장시킨다.
     * @param request
     * @return
     */

    @Transactional
    public BoardDto join(int requestBb, HttpServletRequest request) {
        User user = sessionManager.getSession(request).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_USER));
        Board board;
        Optional<Board> playableBoard = boardRepository.findByTotalPlayerBetween(1, 5);

        if(playableBoard.isPresent())
            board = playableBoard.get();
        else board = Board.builder().blind(1000).phaseStatus(PhaseStatus.WAITING).build();

        Player player = buyIn(board, user, requestBb);
        List<Player> players = board.getPlayers();
        players.add(player);
        seatIn(board, player);

        return new BoardDto(board);
    }

    @Transactional
    public Player buyIn(Board board, User user, int bb) {
        int money = user.getMoney();
        int blind = board.getBlind();
        if(money < blind * bb)
            throw new CustomException(ErrorCode.NOT_ENOUGH_MONEY);

        user.setMoney(user.getMoney() - blind * bb);
        Player player = Player.builder().bb(bb).board(board).status(PlayerStatus.FOLD).build();

        return player;
    }

    public BoardDto startGame(BoardDto boardDto) {
        Board board = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        board.setBtn((board.getBtn() + 1) % MAX_PLAYER);
        board.setActionPos((board.getBtn() + 1) % MAX_PLAYER);
        board.setPhaseStatus(PhaseStatus.PRE_FLOP);

        dealCard(board);
        boardRepository.save(board);
        return new BoardDto(board);
    }
    @Transactional
    public void seatIn(Board board, Player joinPlayer) {
        List<Player> players = board.getPlayers();

        boolean[] isExistSeat = new boolean[MAX_PLAYER];

        for (Player player : players) {
            isExistSeat[player.getPosition().ordinal()] = true;
        }

        Random random = new Random();
        int pos = random.nextInt(MAX_PLAYER);
        for(int i = 0; i < MAX_PLAYER; i++){
            if(isExistSeat[pos]) {
                pos = (pos + 1) % MAX_PLAYER;
            }
            else {
                joinPlayer.setPosition(Position.getPositionByNumber(pos));
                players.add(joinPlayer);
                break;
            }
        }
    }

    @Transactional
    public BoardDto start(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        board.setBtn((board.getBtn() + 1) % MAX_PLAYER);
        dealCard(board);
        board.setPhaseStatus(PhaseStatus.PRE_FLOP);
        board.setActionPos(Position.UTG.getPosNum());
        boardRepository.save(board);

        return new BoardDto(board);
    }

    public void dealCard(Board board) {
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


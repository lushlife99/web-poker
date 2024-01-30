package com.example.pokerv2.service;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Hud;
import com.example.pokerv2.model.Player;
import com.example.pokerv2.repository.BoardRepository;
import com.example.pokerv2.repository.HudRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HudService {

    private final HudRepository hudRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private static final String ACTION_RAISE = "raise";
    private static final String ACTION_FOLD = "fold";
    private static final String ACTION_CALL = "call";


    /**
     *
     * HUD의 각 변수들을 계산해야 하는 시점
     *     private int pfr; -> 프리플랍. 액션을 저장하기 전에.
     *     private int cBet; -> 플랍. 액션을 저장하기 전에.
     *     private int threeBet; -> 모든 페이즈. 액션을 저장하기 전에
     *     private int wsd; -> 팟 분배 이후
     *     private int vpip; -> 프리플랍. PhaseStatus가 플랍으로 변경되기 전에.
     *     private int totalHands; -> 게임 시작 시
     *     private int pfAggressiveCnt; -> 프리플랍. PhaseStatus가 플랍으로 변경되기 전에.
     *     private int wtf; -> 플랍. PhaseStatus가 변경된 직후
     *     private int wtsd; -> 리버 끝.
     */
    public void plusTotalHands(BoardDto boardDto) {
        List<PlayerDto> players = boardDto.getPlayers();
        List<Hud> hudList = new ArrayList<>();
        for (PlayerDto player : players) {
            Optional<Hud> findHud = hudRepository.findByUserId(player.getUserId());

            if(findHud.isPresent()) {
                Hud hud = findHud.get();
                hud.setTotalHands(hud.getTotalHands() + 1);
                hudList.add(hud);
            }
        }
        hudRepository.saveAll(hudList);
    }

    /**
     * totalHands, wtf, wtsd 계산
     * @param boardId
     */
    public void addCountAfterPhaseChange(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();
        List<Hud> hudList = new ArrayList<>();

        PhaseStatus phaseStatus = board.getPhaseStatus();

        if(phaseStatus == PhaseStatus.SHOWDOWN) {
            for (Player player : players) {
                plusWentToShowDown(player);
            }
        } else if(phaseStatus == PhaseStatus.PRE_FLOP) {
            for (Player player : players) {
                plusTotalHands(player);
            }
        } else if(phaseStatus == PhaseStatus.FLOP) {
            for (Player player : players) {
                plusWentToFlop(player);
            }
        }

        hudRepository.saveAll(hudList);
    }

    /**
     * addCountBeforePhaseChange
     *
     * Vpip, PfAggressiveCnt 게산.
     * @param boardId
     */
    public void addCountBeforePhaseChange(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        List<Player> players = board.getPlayers();

        if(board.getPhaseStatus() == PhaseStatus.PRE_FLOP) {
            Player player = players.get(getPlayerIdxByPos(board, board.getBettingPos()));
            plusPfAggressiveCnt(player);
            plusVpipCnt(board);
        }
    }


    /**
     * addCountBeforeSaveAction
     * cBet, 3bet, pfr 계산.
     */
//    public void addCountBeforeSaveAction(BoardDto boardDto, String action) {
//        Board prevboard = boardRepository.findById(boardDto.getId()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
//
//        if(boardDto.getPhaseStatus() == PhaseStatus.PRE_FLOP.ordinal()) {
//            if (action.equals(ACTION_RAISE)) {
//                if(prevboard.getBettingSize() == 0)
//            } else if(action.equals(ACTION_CALL)) {
//
//            }
//        }
//    }

    private void plusVpipCnt(Board board) {
        List<Player> players = board.getPlayers();
        List<Hud> hudList = new ArrayList<>();
        int btnPlayerIdx = getPlayerIdxByPos(board, board.getBtn());
        int sbPlayerIdx = (btnPlayerIdx + 1) % board.getTotalPlayer();
        int bbPlayerIdx = (btnPlayerIdx + 2) % board.getTotalPlayer();

        for(int i = 0; i < board.getTotalPlayer(); i++) {
            Player player = players.get(i);
            int phaseCallSize = player.getPhaseCallSize();
            if(i == sbPlayerIdx) {
                if(board.getBlind() * 0.5 < phaseCallSize) {
                    Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());
                    if(findHud.isPresent()) {
                        Hud hud = findHud.get();
                        hud.setVpip(hud.getVpip() + 1);
                        hudList.add(hud);
                    }
                }
            } else if(i == bbPlayerIdx) {
                if(board.getBlind() < phaseCallSize) {
                    Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());
                    if(findHud.isPresent()) {
                        Hud hud = findHud.get();
                        hud.setVpip(hud.getVpip() + 1);
                        hudList.add(hud);
                    }
                }
            } else {
                if(phaseCallSize > 0) {
                    Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());
                    if(findHud.isPresent()) {
                        Hud hud = findHud.get();
                        hud.setVpip(hud.getVpip() + 1);
                        hudList.add(hud);
                    }
                }
            }
        }

        hudRepository.saveAll(hudList);
    }

    private void plusWentToShowDown(Player player) {
        Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());

        if(findHud.isPresent()) {
            Hud hud = findHud.get();
            hud.setWtsd(hud.getWtsd() + 1);
        }
    }

    private void plusTotalHands(Player player) {
        Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());

        if(findHud.isPresent()) {
            Hud hud = findHud.get();
            hud.setTotalHands(hud.getTotalHands() + 1);
            hudRepository.save(hud);
        }
    }

    private void plusWentToFlop(Player player) {
        Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());

        if (findHud.isPresent()) {
            Hud hud = findHud.get();
            hud.setWtf(hud.getWtf() + 1);
            hudRepository.save(hud);
        }
    }

    private void plusPfAggressiveCnt(Player player) {
        Optional<Hud> findHud = hudRepository.findByUserId(player.getUser().getId());
        if (findHud.isPresent()) {
            Hud hud = findHud.get();
            hud.setPfAggressiveCnt(hud.getPfAggressiveCnt() + 1);
            hudRepository.save(hud);
        }
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
}

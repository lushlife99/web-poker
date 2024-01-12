package com.example.pokerv2.service;


import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class HandCalculator {

    /**
     *
     * 13으로 나눈 나머지 : 카드의 숫자
     * 13으로 나눈 몫 : 카드의 모양
     *
     * 카드의 숫자 -> 2-10: 0-8, J: 9, Q: 10, K: 11, A: 12
     * 카드의 모양 -> 0 : 스페이드, 1 : 다이아, 2 : 하트, 3 : 클로버
     *
     *
     * @param board
     */

    public static int calculate(Board board) {
        List<Player> players = board.getPlayers();
        List<Integer> cards = new ArrayList<>();
        cards.add(board.getCommunityCard1());
        cards.add(board.getCommunityCard2());
        cards.add(board.getCommunityCard3());
        cards.add(board.getCommunityCard4());
        cards.add(board.getCommunityCard5());

        for (Player player : players) {
            cards.add(player.getCard1());
            cards.add(player.getCard2());
            Collections.sort(cards, Collections.reverseOrder());
            if (hasRoyalFlush(cards)) {
                return 900;
            }

            else if (hasStraightFlush(cards)) {
                return 800 + calculateHighCard(cards);
            }

            else if (hasFourOfAKind(cards)) {
                int fourOfAKindValue = calculateFourOfAKindValue(cards);
                int highCard = calculateHighCard(cards);
                return 700 + ((fourOfAKindValue + 1) * 13) + highCard;
            }

            else if (hasFullHouse(cards)) {
                return 600 + calculateFullHouseValue(cards);
            }

            else if (hasFlush(cards)) {
                return 500 + calculateHighCard(cards);
            }

            else if (hasStraight(cards)) {
                return 400 + calculateHighCard(cards);
            }

            else if (hasThreeOfAKind(cards)) {
                return 300 + calculateThreeOfAKindValue(cards);
            }

            else if (hasTwoPair(cards)) {
                return 200 + calculateTwoPairValue(cards);
            }

            else if (hasOnePair(cards)) {
                return 100 + calculateOnePairValue(cards);
            }
        }

        return calculateHighCard(cards);
    }

    private static int calculateOnePairValue(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 2) {
                return rank;
            }
        }

        return -1;
    }

    private static int calculateTwoPairValue(List<Integer> cards) {

        Map<Integer, Integer> rankCount = new HashMap<>();
        List<Integer> pairs = new ArrayList<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 2 && !pairs.contains(rank)) {
                pairs.add(rank);
            }
        }

        if (pairs.size() == 2) {
            return pairs.get(0) + pairs.get(1);
        }

        return -1;
    }

    private static int calculateThreeOfAKindValue(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 3) {
                return rank;
            }
        }

        return -1;

    }

    private static int calculateFullHouseValue(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
        }

        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 3 || entry.getValue() == 2) {
                return entry.getKey();
            }
        }

        return -1;
    }

    private static int calculateFourOfAKindValue(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 4) {
                return rank;
            }
        }

        return -1;
    }

    private static int calculateHighCard(List<Integer> cards) {
        int highestCard = -1;

        for (Integer card : cards) {
            int rank = card % 13;
            highestCard = Math.max(highestCard, rank);
        }

        return highestCard;
    }



    private static boolean hasOnePair(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 2) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasTwoPair(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();
        int pairCount = 0;

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 2) {
                pairCount++;
            }
        }

        return pairCount >= 2;
    }

    private static boolean hasThreeOfAKind(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 3) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasStraight(List<Integer> cards) {
        Set<Integer> uniqueRanks = new HashSet<>();
        for (Integer card : cards) {
            uniqueRanks.add(card % 13);
        }

        List<Integer> sortedUniqueRanks = new ArrayList<>(uniqueRanks);
        Collections.sort(sortedUniqueRanks);

        int consecutiveCount = 1;
        for (int i = 0; i < sortedUniqueRanks.size() - 1; i++) {
            if ((sortedUniqueRanks.get(i) + 1) % 13 == sortedUniqueRanks.get(i + 1)) {
                consecutiveCount++;

                if (consecutiveCount == 5) {
                    return true;
                }
            } else {
                consecutiveCount = 1;
            }
        }

        return false;
    }

    private static boolean hasFlush(List<Integer> cards) {
        Map<Integer, Integer> suitCount = new HashMap<>();

        for (Integer card : cards) {
            int suit = card / 13;
            suitCount.put(suit, suitCount.getOrDefault(suit, 0) + 1);

            if (suitCount.get(suit) == 5) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasFullHouse(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
        }

        boolean hasThreeOfAKind = false;
        boolean hasTwoOfAKind = false;

        for (int count : rankCount.values()) {
            if (count == 3) {
                hasThreeOfAKind = true;
            } else if (count == 2) {
                hasTwoOfAKind = true;
            }
        }

        return hasThreeOfAKind && hasTwoOfAKind;
    }

    private static boolean hasFourOfAKind(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 4) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasStraightFlush(List<Integer> cards) {
        for (int i = 0; i <= cards.size() - 5; i++) {
            boolean isStraightFlush = true;

            for (int j = 0; j < 4; j++) {
                int currentCard = cards.get(i + j);
                int nextCard = cards.get(i + j + 1);

                if ((currentCard % 13) + 1 != nextCard % 13 || currentCard / 13 != nextCard / 13) {
                    isStraightFlush = false;
                    break;
                }
            }

            if (isStraightFlush) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasRoyalFlush(List<Integer> cards) {
        int royalFlushSuit = -1;

        for (Integer card : cards) {
            int rank = card % 13;
            int suit = card / 13;

            if (rank < 9 || rank > 12) {
                return false;
            }

            if (royalFlushSuit == -1) {
                royalFlushSuit = suit;
            } else if (royalFlushSuit != suit) {
                return false;
            }
        }

        return true;
    }

}

package com.example.pokerv2.service;


import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    public int[] calculateValue(Board board) {
        List<Player> players = board.getPlayers();
        List<Integer> cards = new ArrayList<>();
        cards.add(board.getCommunityCard1());
        cards.add(board.getCommunityCard2());
        cards.add(board.getCommunityCard3());
        cards.add(board.getCommunityCard4());
        cards.add(board.getCommunityCard5());
        List<Integer> handValueList = new ArrayList<>(board.getTotalPlayer()); // 각 플레이어의 족보의 세기를 int형식으로 저장.
        List<Integer> jokBoList = new ArrayList<>();

        int cnt = 0;
        for (Player player : players) {
            cards.add(player.getCard1());
            cards.add(player.getCard2());
            Collections.sort(cards, Collections.reverseOrder());

            if(player.getStatus().equals(PlayerStatus.FOLD)) {
                handValueList.add(0);
            }

            int value = royalFlushValue(cards, jokBoList);

            if (value == 0) {
                handValueList.add(90000);
            }

            value = straightFlushValue(cards, jokBoList);

            if (value != -1) {
                handValueList.add(80000 + value);
            }

            value = fourOfAKindValue(cards, jokBoList);

            if(value != -1){
                handValueList.add(70000 + value);
            }

            value = fullHouseValue(cards, jokBoList);

            if (value != -1) {
                handValueList.add(60000 + value);
            }
            /**
             * 24/01/14
             * 여기까지 완료. 다음부터 플러쉬 짜기.
             */

            else if (hasFlush(cards)) {
                handValueList.add(50000 + value);
            }

            else if (hasStraight(cards)) {
                handValueList.add(40000 + value);
            }

            else if (hasThreeOfAKind(cards)) {
                handValueList.add(30000 + value);
            }

            else if (hasTwoPair(cards)) {
                handValueList.add(20000 + value);
            }

            else if (hasOnePair(cards)) {

                handValueList.add(10000 + value);
            }

            else {
                handValueList.add(calculateHighCard(cards, jokBoList));
            }
            cards.remove(5);
            cards.remove(6);
        }

        return null;
    }

    private static int getStraightFlushHighCard(List<Integer> cards) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
        }

        int maxRank = -1;

        for (Integer card : cards) {
            int rank = card % 13;
            if (rankCount.get(rank) >= 5) {
                maxRank = Math.max(maxRank, rank);
            }
        }

        return maxRank;
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
        int[] countArray = new int[13];
        for (int card : cards) {
            int number = card % 13;
            countArray[number]++;
        }
        int fourCardValue = 0;
        int highCardValue = -1;
        int[] result = new int[2];


        for (int i = 0; i < countArray.length; i++) {
            if (countArray[i] == 4) {
                fourCardValue = i;



                for (int j = countArray.length - 1; j >= 0; j--) {
                    if (countArray[j] > 0 && j != i) {
                        highCardValue = j;
                        break;
                    }
                }

                break;
            }
        }

        return (fourCardValue + 1) * 13 + highCardValue;
    }

    private static int calculateHighCard(List<Integer> cards, List<Integer> jokBo) {
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

    /**
     * fullHouseValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 풀하우스의 밸류. (트리플의 밸류(우선순위) && 페어의 밸류)
     */
    private static int fullHouseValue(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
        }

        boolean hasThreeOfAKind = false;
        boolean hasTwoOfAKind = false;
        int threeOfKindValue = -1;
        int twoOfKindValue = -1;

        for (Map.Entry<Integer, Integer> entry : rankCount.entrySet()) {
            int count = entry.getValue();
            int rank = entry.getKey();

            if (count == 3) {
                threeOfKindValue = rank;
                hasThreeOfAKind = true;
            } else if (count == 2) {
                twoOfKindValue = rank;
                hasTwoOfAKind = true;
            }
        }

        if (hasThreeOfAKind && hasTwoOfAKind) {
            jokBo.clear();
            for (Integer card : cards) {
                int rank = card % 13;
                if (rank == threeOfKindValue) {
                    jokBo.add(card);
                }
            }

            for (Integer card : cards) {
                int rank = card % 13;
                if (rank == twoOfKindValue) {
                    jokBo.add(card);
                    if(jokBo.size() == 5)
                        break;
                }
            }
            return (threeOfKindValue + 1) * 100 + twoOfKindValue;
        }

        return -1;
    }

    /**
     * fourOfAKindValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 포카드의 밸류. (포카드의 밸류 && 포카드를 제외한 나머지 카드중 하이카드)
     */
    private static int fourOfAKindValue(List<Integer> cards, List<Integer> jokBo) {
        int[] ranks = new int[13];
        for (int card : cards) {
            ranks[card%13]++;
        }

        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == 4) {
                int fourAKindValue = -1;
                int highCardValue = -1;
                for (int j = 0; j < cards.size(); j++) {
                    int card = cards.get(j);
                    if (card % 13 == i) {
                        fourAKindValue = i;
                        jokBo.add(card);
                    }
                }

                for (Integer card : cards) {
                    if(card % 13 != fourAKindValue){
                        highCardValue = card % 13;
                        break;
                    }
                }
                return (fourAKindValue + 1) * 100 + highCardValue;
            }
        }

        return -1;
    }

    /**
     * straightFlushValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 스트레이트 플러시의 밸류. (스티플의 하이카드)
     */
    private static int straightFlushValue(List<Integer> cards, List<Integer> jokBo) {

        int value = -1;

        for (int i = 0; i <= cards.size() - 5; i++) {
            boolean isStraightFlush = true;

            for (int j = 0; j < 4; j++) {
                int currentCard = cards.get(i + j);
                int nextCard = cards.get(i + j + 1);

                if ((currentCard % 13) - 1 != nextCard % 13 || currentCard / 13 != nextCard / 13) {
                    jokBo.clear();
                    isStraightFlush = false;
                    value = -1;
                    break;
                }
                jokBo.add(currentCard);
                if(j == 3){
                    value = j + 4;
                    jokBo.add(nextCard);
                }
            }

            if (isStraightFlush) {
                return value;
            }
        }
        return -1;
    }

    /**
     * royalFlushValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 로티플 true -> 0. false -> -1
     */
    private static int royalFlushValue(List<Integer> cards, List<Integer> jokBo) {

        List<Integer> list = Arrays.asList(8, 9, 10, 11, 12);
        Collections.sort(cards);
        int cnt = 0;
        while(true) {
            if (cards.containsAll(list)) {
                jokBo = List.copyOf(list);
                return 0;
            } else {
                list = list.stream()
                        .map(card -> card + 13)
                        .collect(Collectors.toList());
                if(cnt == 4){
                    break;
                }
            }
        }

        return -1;
    }
}

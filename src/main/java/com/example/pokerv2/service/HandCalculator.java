package com.example.pokerv2.service;


import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HandCalculator {

    /**
     * 24/01/15 chan
     *
     * 카드가 저장된 방법.
     * 13으로 나눈 나머지 : 카드의 숫자
     * 13으로 나눈 몫 : 카드의 모양
     * <p>
     * 카드의 숫자 -> 2-10: 0-8, J: 9, Q: 10, K: 11, A: 12
     * 카드의 모양 -> 0 : 스페이드, 1 : 다이아, 2 : 하트, 3 : 클로버
     *
     * 함수가 너무 긺. 나중에 리팩토링 하기.
     * @param board
     * @return 각 플레이어들의 족보의 세기를 담은 리스트
     */

    public static long[][] calculateValue(Board board) {
        long[][] valueAndJokBo = new long[board.getTotalPlayer()][];
        for (long[] longs : valueAndJokBo) {
            longs = new long[6];
        }
        List<Integer> cards = new ArrayList<>();
        cards.add(board.getCommunityCard1());
        cards.add(board.getCommunityCard2());
        cards.add(board.getCommunityCard3());
        cards.add(board.getCommunityCard4());
        cards.add(board.getCommunityCard5());
        int cnt = 0;
        List<Long> handValueList = new ArrayList<>(board.getTotalPlayer()); // 각 플레이어의 족보의 세기를 long 형식으로 저장

        for (Player player : board.getPlayers()) {
            List<Integer> jokBoList = new ArrayList<>();
            cards.add(player.getCard1());
            cards.add(player.getCard2());
            Collections.sort(cards, Collections.reverseOrder());

            if (player.getStatus().equals(PlayerStatus.FOLD)) {
                handValueList.add(0L);
            } else {
                long value = royalFlushValue(cards, jokBoList);

                if (value != 1) {
                    handValueList.add(90000000000L);
                } else {
                    value = straightFlushValue(cards, jokBoList);

                    if (value != -1) {
                        handValueList.add(80000000000L + value);
                    } else {
                        value = fourOfAKindValue(cards, jokBoList);

                        if (value != -1) {
                            handValueList.add(70000000000L + value);
                        } else {
                            value = fullHouseValue(cards, jokBoList);

                            if (value != -1) {
                                handValueList.add(60000000000L + value);
                            } else {
                                value = flushValue(cards, jokBoList);

                                if (value != -1) {
                                    handValueList.add(50000000000L + value);
                                } else {
                                    value = straightValue(cards, jokBoList);

                                    if (value != -1) {
                                        handValueList.add(40000000000L + value);
                                    } else {
                                        value = threeOfAKindValue(cards, jokBoList);

                                        if (value != -1) {
                                            handValueList.add(30000000000L + value);
                                        } else {
                                            value = twoPairValue(cards, jokBoList);

                                            if (value != -1) {
                                                handValueList.add(20000000000L + value);
                                            } else {
                                                value = onePairValue(cards, jokBoList);

                                                if (value != -1) {
                                                    handValueList.add(10000000000L + value);
                                                } else {
                                                    handValueList.add(highCardValue(cards, jokBoList));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            cards.remove(5);
            cards.remove(6);
            valueAndJokBo[cnt][0] = handValueList.get(cnt);
            valueAndJokBo[cnt][1] = jokBoList.get(0);
            valueAndJokBo[cnt][2] = jokBoList.get(2);
            valueAndJokBo[cnt][3] = jokBoList.get(3);
            valueAndJokBo[cnt][4] = jokBoList.get(4);
            valueAndJokBo[cnt][5] = jokBoList.get(5);
            cnt++;

            for (Integer integer : jokBoList) {
                valueAndJokBo[cnt][1] = handValueList.get(cnt);
            }
        }

        return valueAndJokBo;
    }


    /**
     * highCardValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 하이카드의 밸류 (5장의 하이카드)
     */
    private static long highCardValue(List<Integer> cards, List<Integer> jokBo) {
        Collections.sort(cards, Comparator.comparingInt(o -> o % 13));

        jokBo.clear();
        int cnt = 0;
        for (Integer card : cards) {
            jokBo.add(card);
            cnt++;
            if (cnt == 5) {
                break;
            }
        }

        return (jokBo.get(0) % 13 + 1) * 100000000L + (jokBo.get(1) % 13 + 1) * 1000000L + (jokBo.get(2) % 13 + 1) * 10000L + (jokBo.get(3) % 13 + 1) * 100L + (jokBo.get(4) % 13 + 1);
    }

    /**
     * onePairValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 원페어의 밸류 (원페어의 하이카드, 나머지 3장의 하이카드)
     */
    private static long onePairValue(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> rankCount = new HashMap<>();
        int pairRank = -1;

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 2) {
                jokBo.clear();
                pairRank = rank;
                for (Integer pairCard : cards) {
                    if (pairCard % 13 == pairRank) {
                        jokBo.add(pairCard);

                    }
                }
            }
        }

        Collections.sort(cards, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 % 13 - o1 % 13;
            }
        });

        int cnt = 0;
        if (pairRank != -1) {
            for (Integer card : cards) {
                if (card % 13 != pairRank) {
                    if (cnt++ < 3) {
                        jokBo.add(card);
                    } else break;
                }
            }
            return (pairRank + 1) * 1000000L + (jokBo.get(2) % 13 + 1) * 10000L + (jokBo.get(3) % 13 + 1) * 100L + (jokBo.get(4) % 13 + 1);
        }

        return -1;
    }

    /**
     * twoPairValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 투 페어의 밸류 (두 페어의 rank + 나머지 1개 하이카드)
     */
    private static long twoPairValue(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> rankCount = new HashMap<>();
        int firstPairRank = -1;
        int secondPairRank = -1;
        int highCard = -1;

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 2) {
                if (firstPairRank == -1) {
                    firstPairRank = rank;
                } else {
                    secondPairRank = rank;
                }
            } else if (highCard % 13 < rank) {
                highCard = card;
            }
        }

        if (firstPairRank != -1 && secondPairRank != -1) {
            jokBo.clear();
            for (Integer pairCard : cards) {
                int rank = pairCard % 13;
                if (rank == firstPairRank || rank == secondPairRank) {
                    jokBo.add(pairCard);
                }
            }
            jokBo.add(highCard);
            return (Math.max(firstPairRank, secondPairRank) + 1) * 10000L + (Math.min(firstPairRank, secondPairRank) + 1) * 100L + (highCard % 13 + 1);
        }

        return -1;
    }

    /**
     * threeOfAKindValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 트리플의 밸류 (트리플의 rank + 족보의 나머지 2개 하이카드)
     */
    private static long threeOfAKindValue(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> rankCount = new HashMap<>();
        int threeOfAKindValue = -1;
        int firstHighCard = -1;
        int secondHighCard = -1;
        boolean isThreeOfAKind = false;

        for (Integer card : cards) {
            int rank = card % 13;
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);

            if (rankCount.get(rank) == 3) {
                jokBo.clear();
                int tripleRank = rank;
                for (Integer tripleCard : cards) {
                    if (tripleCard % 13 == tripleRank) {
                        jokBo.add(tripleCard);
                    }
                }

                isThreeOfAKind = true;
                threeOfAKindValue = tripleRank;
            } else if (secondHighCard % 13 < rank) {
                if (firstHighCard % 13 < rank) {
                    secondHighCard = firstHighCard;
                    firstHighCard = card;
                } else {
                    secondHighCard = card;
                }
            }
        }

        if (isThreeOfAKind) {
            jokBo.add(firstHighCard);
            jokBo.add(secondHighCard);
            return (threeOfAKindValue + 1) * 10000L + (firstHighCard % 13 + 1) * 100L + (secondHighCard % 13 + 1);
        }

        return -1;
    }


    /**
     * straightValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 스트레이트의 밸류 (스트레이트를 이루는 족보중 하이카드)
     */
    private static long straightValue(List<Integer> cards, List<Integer> jokBo) {
        int consecutiveCount = 1;
        int lastRank = -1;
        int highestRank = -1;

        for (Integer card : cards) {
            int currentRank = card % 13;

            if (lastRank != -1 && (lastRank + 1) % 13 == currentRank) {
                consecutiveCount++;
                jokBo.add(card);

                if (consecutiveCount == 5) {
                    highestRank = currentRank + 4;
                    break;
                }
            } else if (lastRank != currentRank) {
                consecutiveCount = 1;
                jokBo.clear();
                jokBo.add(card);
            }

            lastRank = currentRank;
        }

        return highestRank;
    }


    /**
     * getCardWithRank
     *
     * @param cards 카드 리스트.
     * @param rank  찾고자 하는 랭크.
     * @return 주어진 랭크를 가진 카드.
     */
    private static int getCardWithRank(List<Integer> cards, int rank) {
        for (Integer card : cards) {
            if (card % 13 == rank) {
                return card;
            }
        }
        return -1;
    }

    /**
     * flushValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 플러시의 밸류. (플러시를 이루는 카드의 하이카드 순서)
     */

    private static long flushValue(List<Integer> cards, List<Integer> jokBo) {
        Map<Integer, Integer> suitCount = new HashMap<>();

        for (Integer card : cards) {
            int suit = card / 13;
            suitCount.put(suit, suitCount.getOrDefault(suit, 0) + 1);

            if (suitCount.get(suit) == 5) {
                jokBo.clear();
                for (Integer flushCard : cards) {
                    if (flushCard / 13 == suit) {
                        jokBo.add(flushCard);
                        if (jokBo.size() == 5)
                            break;
                    }
                }

                Collections.sort(jokBo, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o2 % 13 - o1 % 13;
                    }
                });

                long flushValue = 0L;
                long valuePrefix = 10000000000L;
                for (int i = 0; i < jokBo.size(); i++) {
                    flushValue = flushValue + ((jokBo.get(i) % 13) + 1) * valuePrefix;
                    valuePrefix /= 100L;
                }
                return flushValue;
            }
        }

        return -1;
    }

    /**
     * fullHouseValue
     *
     * @param cards 내림차 순으로 정렬된 카드 리스트.
     * @param jokBo 만약 조건이 만족할 경우 족보를 이루는 5개의 카드를 담아줌.
     * @return 풀하우스의 밸류. (트리플의 밸류(우선순위) && 페어의 밸류)
     */
    private static long fullHouseValue(List<Integer> cards, List<Integer> jokBo) {
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
                    if (jokBo.size() == 5)
                        break;
                }
            }
            return (threeOfKindValue + 1) * 100 + twoOfKindValue + 1;
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
    private static long fourOfAKindValue(List<Integer> cards, List<Integer> jokBo) {
        int[] ranks = new int[13];
        for (int card : cards) {
            ranks[card % 13]++;
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
                    if (card % 13 != fourAKindValue) {
                        highCardValue = card % 13;
                        break;
                    }
                }
                return (fourAKindValue + 1) * 100 + highCardValue + 1;
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
    private static long straightFlushValue(List<Integer> cards, List<Integer> jokBo) {

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
                if (j == 3) {
                    value = j + 4;
                    jokBo.add(nextCard);
                }
            }

            if (isStraightFlush) {
                return value + 1;
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
    private static long royalFlushValue(List<Integer> cards, List<Integer> jokBo) {

        List<Integer> list = Arrays.asList(8, 9, 10, 11, 12);
        Collections.sort(cards);
        int cnt = 0;
        while (true) {
            if (cards.containsAll(list)) {
                jokBo = List.copyOf(list);
                return 0;
            } else {
                list = list.stream()
                        .map(card -> card + 13)
                        .collect(Collectors.toList());
                if (cnt == 4) {
                    break;
                }
            }
        }

        return -1;
    }
}

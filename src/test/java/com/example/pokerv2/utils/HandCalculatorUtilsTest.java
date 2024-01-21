package com.example.pokerv2.utils;

import com.example.pokerv2.dto.GameResultDto;
import com.example.pokerv2.enums.HandValue;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class HandCalculatorUtilsTest {

    private static final int CARD_SIZE = 7;

    @Test
    @DisplayName("로얄 스트레이트 플러시 검증 체크")
    void evaluateRoyalStraightFlushTest() {

        //given
        ArrayList<Integer> royalStraightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("A","d"),
                CardUtils.getCardValue("K","d"),
                CardUtils.getCardValue("Q","d"),
                CardUtils.getCardValue("J","d"),
                CardUtils.getCardValue("10","d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(royalStraightFlushJokBo);
        cards.addAll(royalStraightFlushJokBo);

        cards.add(CardUtils.getCardValue("2","s"));
        cards.add(CardUtils.getCardValue("3","s"));



        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.ROYAL_STRAIGHT_FLUSH.getDetail());
        assertThat(royalStraightFlushJokBo).isEqualTo(jokBo);

        printTestDetails(cards, jokBo);

    }

    @Test
    @DisplayName("스트레이트 플러시 검증 체크")
    void evaluateStraightFlushTest() {

        //given
        ArrayList<Integer> straightFlushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10","d"),
                CardUtils.getCardValue("9","d"),
                CardUtils.getCardValue("8","d"),
                CardUtils.getCardValue("7","d"),
                CardUtils.getCardValue("6","d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightFlushJokBo);
        cards.addAll(straightFlushJokBo);

        cards.add(CardUtils.getCardValue("2","s"));
        cards.add(CardUtils.getCardValue("3","s"));


        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT_FLUSH.getDetail());
        assertThat(straightFlushJokBo).isEqualTo(jokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("포카드 검증 체크")
    void evaluateFourOfAKindTest() {

        //given
        ArrayList<Integer> fourOfAKindJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("10", "s"),
                CardUtils.getCardValue("10", "c"),
                CardUtils.getCardValue("A", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(fourOfAKindJokBo);
        cards.addAll(fourOfAKindJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.FOUR_OF_A_KIND.getDetail());
        assertThat(fourOfAKindJokBo).isEqualTo(jokBo);

        printTestDetails(cards, jokBo);

    }

    @Test
    @DisplayName("풀하우스 검증 체크")
    void evaluateFullHouseTest() {

        //given
        ArrayList<Integer> fullHouseJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("10", "s"),
                CardUtils.getCardValue("3", "c"),
                CardUtils.getCardValue("3", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(fullHouseJokBo);
        cards.addAll(fullHouseJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("5", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.FULL_HOUSE.getDetail());
        assertThat(fullHouseJokBo).isEqualTo(jokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("플러시 검증 체크")
    void evaluateFlushTest() {

        //given
        ArrayList<Integer> flushJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("A", "h"),
                CardUtils.getCardValue("K", "h"),
                CardUtils.getCardValue("Q", "h"),
                CardUtils.getCardValue("10", "h"),
                CardUtils.getCardValue("7", "h")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(flushJokBo);
        cards.addAll(flushJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.FLUSH.getDetail());
        assertThat(flushJokBo).isEqualTo(jokBo);

        printTestDetails(cards, jokBo);
    }

    @Test
    @DisplayName("스트레이트 검증 체크")
    void evaluateStraightTest() {

        //given
        ArrayList<Integer> straightJokBo = new ArrayList<>(List.of(
                CardUtils.getCardValue("10", "d"),
                CardUtils.getCardValue("9", "h"),
                CardUtils.getCardValue("8", "s"),
                CardUtils.getCardValue("7", "c"),
                CardUtils.getCardValue("6", "d")
        ));
        ArrayList<Integer> cards = new ArrayList<>(CARD_SIZE);

        Collections.sort(straightJokBo);
        cards.addAll(straightJokBo);

        cards.add(CardUtils.getCardValue("2", "s"));
        cards.add(CardUtils.getCardValue("3", "s"));

        //when
        GameResultDto gameResultDto = HandCalculatorUtils.calculateValue(cards);
        List<Integer> jokBo = gameResultDto.getJokBo();
        Collections.sort(jokBo);

        //then
        assertThat(HandCalculatorUtils.getHandContextByValue(gameResultDto.getHandValue())).isEqualTo(HandValue.STRAIGHT.getDetail());
        assertThat(jokBo).isEqualTo(straightJokBo);

        printTestDetails(cards, jokBo);
    }

    public static void printTestDetails(List<Integer> cards, List<Integer> jokBo){
        ArrayList<String> cardContexts = new ArrayList<>();
        for (Integer card : cards) {
            cardContexts.add(CardUtils.getCardContext(card));
        }
        log.info("\nCard List: {}", cardContexts);
        cardContexts.clear();

        for (Integer card : jokBo) {
            cardContexts.add(CardUtils.getCardContext(card));
        }
        log.info("\nJokBo List: {}", cardContexts);
    }

}
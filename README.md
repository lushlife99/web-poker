# Poker Game

<div align="center">
  
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data JPA">
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=docker&logoColor=white" alt="Spring Security">
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white" alt="WebSocket">

<h3>실시간 최대 6인 멀티플레이어 포커 게임</h3>

</div>

## 📌 프로젝트 개요

**Poker Game**은 실시간 웹소켓 통신을 활용한 멀티플레이어 포커 게임입니다.

- **개발 기간**: 2024.01 ~ 2024.02
- **개발 인원**: 2명 (본인: 백엔드 담당)
- **프로젝트 배경**: 
  - 개발 공부를 처음 시작했을 때 HTTP 통신만으로 포커 게임을 구현하려 했습니다.
  - 게임 동작의 실시간성이 부족하여 WebSocket과 Stomp를 도입, 웹소켓 통신으로 최대 6명이 동시 플레이 가능한 포커 게임을 구현했습니다.

## 👨‍💻 참여자

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/lushlife99">
        <img src="https://avatars.githubusercontent.com/lushlife99" width="160px" alt="정찬"/>
        <br />
        <sub><b>정찬</b></sub>
      </a>
      <br />
      <sub>백엔드</sub>
    </td>
    <td align="center">
      <a href="https://github.com/hyomin1">
        <img src="https://avatars.githubusercontent.com/hyomin1" width="160px" alt="이효민"/>
        <br />
        <sub><b>이효민</b></sub>
      </a>
      <br />
      <sub>프론트엔드</sub>
    </td>
  </tr>
</table>

## 흐름도

### 1. 포커 게임 흐름도
<img width="3779" height="485" alt="포커" src="https://github.com/user-attachments/assets/601b1501-e63d-4c59-8ebe-eec597bb8815" />

### 2. 플레이어 자리 비움 흐름도
<img width="2217" height="334" alt="포커 (1)" src="https://github.com/user-attachments/assets/d0e0ef3c-3e75-4a65-bbe0-06fb83e4e9e5" />



## 🚀 주요 기능

### 1. 실시간 멀티플레이어 게임 로직 구현 (최대 6명 동시 플레이)

- WebSocket 기반으로 실시간 게임 상태 동기화
- 턴 기반 게임 로직 및 플레이어 간 실시간 상호작용 구현
- 포커 게임 규칙에 따른 베팅, 콜, 레이즈, 폴드 등 다양한 액션 처리
- 실시간 게임 진행 상황 및 결과 표시

### 2. 사용자 인증 및 게임룸 관리 시스템

- JWT 토큰 기반 사용자 인증 구현
- 게임룸 생성, 참여, 퇴장 기능 개발
- 블라인드 별 게임룸 분류 및 선택 기능

### 3. 게임 통계 및 플레이어 데이터 관리

- 플레이어별 HUD(Heads-Up Display) 통계 정보 제공
- 핸드 히스토리 기록 및 조회 기능
- 게임 결과에 따른 플레이어 정보 업데이트

## 📱 서비스

<table>
  <tr>
    <td align="center"><b>로그인</b></td>
  </tr>
  <tr>
    <td>
      <img width="100%" src="https://github.com/hyomin1/poker/assets/98298940/1fb29e2f-5ef1-47e7-976b-8e0ab52666e1"/>
    </td>
  </tr>
  
  <tr>
    <td align="center"><b>이미지 업로드</b></td>
  </tr>
  <tr>
    <td>
      <img width="100%" src="https://github.com/hyomin1/poker/assets/98298940/213257bb-5cd6-4ff2-b7f2-a087184f238f"/>
    </td>
  </tr>
  
  <tr>
    <td align="center"><b>메인화면</b></td>
  </tr>
  <tr>
    <td>
      <img width="100%" src="https://github.com/hyomin1/poker/assets/98298940/f093b095-fd50-48ca-baa8-dcab0c5fa3e1"/>
    </td>
  </tr>
  
  <tr>
    <td align="center"><b>게임플레이</b></td>
  </tr>
  <tr>
    <td>
      <img width="100%" src="https://github.com/hyomin1/poker/assets/98298940/ae2c5cc7-230f-4692-b27b-f8321e27aa74"/>
    </td>
  </tr>
  
  <tr>
    <td align="center"><b>게임결과 1</b></td>
  </tr>
  <tr>
    <td>
      <img width="100%" src="https://github.com/hyomin1/poker/assets/98298940/726b5241-5725-4911-8a65-c3741935c163"/>
    </td>
  </tr>
  
  <tr>
    <td align="center"><b>게임결과 2</b></td>
  </tr>
  <tr>
    <td>
      <img width="100%" src="https://github.com/hyomin1/poker/assets/98298940/566665aa-c14e-4d90-a427-acc76c4c2f64"/>
    </td>
  </tr>
  
  <tr>
    <td align="center"><b>HUD</b></td>
  </tr>
  <tr>
    <td>
      <img width="100%" src="https://github.com/hyomin1/poker/assets/98298940/f9768842-c59c-43be-84e5-03001602d415"/>
    </td>
  </tr>
  
  <tr>
    <td align="center"><b>핸드히스토리</b></td>
  </tr>
  <tr>
    <td>
      <img width="100%" src="https://github.com/hyomin1/poker/assets/98298940/34c25555-29bb-4299-b1f2-9af0e8116c60"/>
    </td>
  </tr>
</table>

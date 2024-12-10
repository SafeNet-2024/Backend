> [프로젝트 설명 바로 가기](https://github.com/SafeNet-2024)

## 👥 Members and Roles

| 이름        | 역할               | 담당 파트           |
|-------------|--------------------|---------------------|
| [노경희](https://github.com/khee2) | Backend            | 게시글, 채팅  |
| [박채연](https://github.com/Yeon-chae) | Backend            | 회원, 영수증 판별  |

## 📂 Project Folder Structure

```plaintext
Backend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.SafeNet.Backend
│   │   │       ├── domain                     # 주요 도메인 로직
│   │   │       │   ├── file                   # 파일 관련 도메인
│   │   │       │   │   ├── controller
│   │   │       │   │   ├── entity
│   │   │       │   │   ├── repository
│   │   │       │   │   └── service
│   │   │       │   ├── member                 # 사용자 관련 도메인
│   │   │       │   ├── message                # 메시징 관련 도메인
│   │   │       │   ├── messageroom            # 채팅방 관련 도메인
│   │   │       │   └── post                   # 게시글 관련 도메인
│   │   │       │   └── postLike               # 게시글 좋아요 관련 도메인
│   │   │       │   └── region                 # 지역 관련 도메인
│   │   │       ├── global                     # 전역 설정 및 유틸리티
│   │   │       │   ├── auth                   # JWT 인증 및 보안
│   │   │       │   ├── config                 # 전역 설정 (Redis, S3, Swagger 등)
│   │   │       │   ├── exception              # 예외 처리 핸들링
│   │   │       │   ├── pubsub                 # Redis Pub/Sub
│   │   │       │   └── util                   # 유틸리티 클래스
│   │   └── resources
│   │       ├── application.yml                # 설정 파일(현재 깃이그노어에 등록됨)
├── build.gradle
├── Dockerfile
├── README.md
└── settings.gradle
```

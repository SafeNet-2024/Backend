# 🌿 𝗚𝗿𝗼𝗦𝗵𝗮𝗿𝗲-𝗕𝗮𝗰𝗸𝗲𝗻𝗱 🌿

> [GroShare 프로젝트 설명 바로 가기](https://github.com/SafeNet-2024)

## 👥 𝗠𝗲𝗺𝗯𝗲𝗿𝘀 𝗮𝗻𝗱 𝗥𝗼𝗹𝗲𝘀

| 이름        | 역할               | 담당 파트           |
|-------------|--------------------|---------------------|
| [노경희](https://github.com/khee2) | Backend            | 게시글, 채팅  |
| [박채연](https://github.com/Yeon-chae) | Backend            | 회원, 영수증 판별  |

## 📂 𝗣𝗿𝗼𝗷𝗲𝗰𝘁 𝗙𝗼𝗹𝗱𝗲𝗿 𝗦𝘁𝗿𝘂𝗰𝘁𝘂𝗿𝗲

```plaintext
Backend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.SafeNet.Backend
│   │   │       ├── domain                   
│   │   │       │   ├── file                 
│   │   │       │   │   ├── controller
│   │   │       │   │   ├── entity
│   │   │       │   │   ├── repository
│   │   │       │   │   └── service
│   │   │       │   ├── member              
│   │   │       │   ├── message            
│   │   │       │   ├── messageroom          
│   │   │       │   └── post                
│   │   │       │   └── postLike             
│   │   │       │   └── region                
│   │   │       ├── global                     
│   │   │       │   ├── auth                   
│   │   │       │   ├── config               
│   │   │       │   ├── exception              
│   │   │       │   ├── pubsub              
│   │   │       │   └── util                  
│   │   └── resources
│   │       ├── application.yml                # 설정 파일(현재 깃이그노어에 등록됨)
├── build.gradle
├── Dockerfile
├── README.md
└── settings.gradle
```

# _nolanbot

**한국어** | [English](#english)

카카오톡, 텔레그램, LINE, Messenger, Instagram 메시지를 **JavaScript로 자동화**하는 메신저 봇 플랫폼입니다.
안드로이드 폰이 봇 서버 역할을 하며, 웹 대시보드에서 스크립트를 작성·배포하고 실시간으로 모니터링합니다.

---

## 목차

- [작동 방식](#작동-방식)
- [주요 기능](#주요-기능)
- [서버 설치](#서버-설치-linux--docker)
- [안드로이드 앱 설치](#안드로이드-앱-설치)
- [앱-서버 페어링](#앱-서버-페어링)
- [봇 스크립트 작성](#봇-스크립트-작성)
- [스크립트 API](#스크립트-api)
- [지원 메신저](#지원-메신저)
- [기술 스택](#기술-스택)
- [문제 해결](#문제-해결)

---

## 작동 방식

```
[카카오톡 / 텔레그램 / LINE 등]
          ↓ 알림 수신
   [안드로이드 폰]
   · 알림 가로채기 (NotificationListenerService)
   · JavaScript 스크립트 실행 (Mozilla Rhino 엔진)
   · 자동 답장 / SMS 발송 / 전화 걸기
          ↕ Socket.IO (실시간 양방향)
   [클라우드 서버 (Node.js + MongoDB)]
          ↕
   [웹 대시보드 (React)]
   · 스크립트 에디터 (Monaco Editor)
   · 실시간 메시지 로그
   · 기기 관리 및 모니터링
```

1. 메신저 앱에서 알림이 오면 안드로이드 앱이 가로챕니다
2. 등록된 JavaScript 스크립트가 실행됩니다
3. 스크립트에서 답장, SMS, 전화, HTTP 요청 등을 처리합니다
4. 모든 활동은 웹 대시보드에서 실시간으로 확인 가능합니다

---

## 주요 기능

- ✅ **메시지 자동응답** — 카카오톡, 텔레그램 등에서 조건에 맞는 답장 자동 전송
- ✅ **자동 전화 걸기** — 특정 메시지 수신 시 지정 번호로 자동 전화
- ✅ **자동 SMS 발송** — 스크립트에서 문자 메시지 전송
- ✅ **HTTP API 연동** — 외부 API 호출 및 응답 처리
- ✅ **원격 스크립트 배포** — 웹 대시보드에서 수정하면 폰에 즉시 반영
- ✅ **실시간 로그** — 수신/발신 메시지 실시간 모니터링
- ✅ **다중 기기 관리** — 여러 안드로이드 폰을 하나의 대시보드에서 관리
- ✅ **앱 종료 후에도 동작** — 백그라운드 포그라운드 서비스로 상시 실행

---

## 서버 설치 (Linux + Docker)

### 필수 조건

- Ubuntu 20.04 이상
- Docker Engine 24.0 이상

### 1. Docker 설치

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker
```

### 2. 프로젝트 클론

```bash
git clone https://github.com/NolanKiwi/megabot.git
cd megabot
```

### 3. 환경변수 설정

```bash
cp server/.env.example server/.env
nano server/.env
```

아래 내용으로 수정하세요:

```env
PORT=3000
MONGODB_URI=mongodb://mongodb:27017/megabot
JWT_ACCESS_SECRET=여기에-랜덤-시크릿-키-입력-32자이상
JWT_REFRESH_SECRET=여기에-다른-랜덤-시크릿-키-입력-32자이상
JWT_ACCESS_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d
CORS_ORIGIN=*
```

> ⚠️ JWT 시크릿 키는 반드시 변경하세요:
> ```bash
> openssl rand -hex 32
> ```

### 4. 서버 실행

```bash
docker compose up -d
```

### 5. 정상 동작 확인

```bash
docker compose ps
# 3개 컨테이너 (server, web, mongodb) 모두 Up 상태 확인

curl http://localhost:3000/api/health
# {"status":"ok",...}
```

웹 대시보드 접속: **http://서버IP:5173**

### 6. 방화벽 포트 개방

```bash
sudo ufw allow 3000   # API 서버
sudo ufw allow 5173   # 웹 대시보드
```

### 업데이트 방법

```bash
git pull
docker compose up --build -d
```

---

## 안드로이드 앱 설치

### 필수 조건

- Android Studio (최신 버전)
- Android 8.0 (API 26) 이상 기기
- USB 디버깅 활성화된 안드로이드 폰

### 빌드 및 설치

1. [Android Studio](https://developer.android.com/studio) 설치
2. `android/` 폴더를 Android Studio로 열기
3. Gradle Sync 완료 대기
4. 폰을 USB로 연결 → USB 디버깅 ON
5. 상단 ▶ **Run** 버튼 클릭

앱이 자동으로 빌드되어 폰에 설치됩니다.

### 필수 권한 허용

앱 설치 후 **Permissions 탭**에서 모두 허용:

| 권한 | 용도 |
|------|------|
| 알림 접근 | 메신저 메시지 수신 (가장 중요) |
| 전화 걸기 | 자동 전화 기능 |
| SMS 전송/읽기 | 자동 문자 기능 |
| 전화 상태 읽기 | 통화 상태 확인 |

> ⚠️ **알림 접근 권한**은 설정 → 앱 → 특별한 앱 접근 → 알림 접근에서 허용해야 합니다.

### 배터리 최적화 제외 (필수)

앱이 백그라운드에서 계속 실행되려면:

**설정 → 배터리 → 배터리 최적화 → _nolanbot → 최적화 안 함**

삼성 폰의 경우 추가로:

**설정 → 앱 → _nolanbot → 배터리 → 제한 없음**

---

## 앱-서버 페어링

### 1단계 — 페어링 코드 발급 (웹 대시보드)

1. **http://서버IP:5173** 접속
2. 계정 생성 후 로그인
3. **Devices 탭** → **Generate Pairing Code** 클릭
4. 6자리 코드 복사 (10분간 유효)

### 2단계 — 앱 연결 (안드로이드)

1. _nolanbot 앱 실행
2. **Settings 탭** 이동
3. **Server URL**: `http://서버IP:3000` 입력
4. **Pairing Code**: 복사한 6자리 코드 입력
5. **Connect to Cloud** 버튼 클릭
6. "Connected" 표시 확인

### 3단계 — Bot Service 시작

1. **Home 탭** → **Bot Service** 스위치 **ON**
2. 상태바에 "_nolanbot - Bot is running" 알림 확인

> 💡 Bot Service를 한 번 켜면 앱을 닫아도 백그라운드에서 계속 실행됩니다.

---

## 봇 스크립트 작성

### 스크립트 만들기

**방법 1 — 앱에서 직접**
- Scripts 탭 → **+** 버튼 → 코드 작성 → Save

**방법 2 — 웹 대시보드에서 원격 배포**
- http://서버IP:5173/scripts → **New** → 코드 작성 → **Save & Deploy**
- 저장 즉시 연결된 모든 폰에 자동 배포됩니다

### 스크립트 구조

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    // room       : 채팅방 이름
    // msg        : 수신된 메시지 내용
    // sender     : 보낸 사람 이름
    // isGroupChat: 그룹 채팅 여부 (true/false)
    // replier    : 답장 객체
    // packageName: 앱 패키지명 (예: org.telegram.messenger)

    if (msg == "안녕") {
        replier.reply("안녕하세요!");
    }
}
```

### 스크립트 예제

#### 기본 자동 답장

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "ping") {
        replier.reply("pong");
    }

    if (msg == "안녕") {
        replier.reply("안녕하세요! 봇입니다 🤖");
    }
}
```

#### 특정 메시지 오면 전화 걸기

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "긴급") {
        Phone.call("010-1234-5678");
        replier.reply("전화 연결 중...");
    }
}
```

#### 특정 메시지 오면 SMS 발송

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "알림") {
        Sms.send("010-1234-5678", sender + "님이 메시지를 보냈습니다: " + msg);
    }
}
```

#### HTTP API 연동

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "날씨") {
        var res = Http.requestSync("https://wttr.in/Seoul?format=3");
        replier.reply("현재 날씨: " + res.body);
    }
}
```

#### 특정 사람 메시지만 처리

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (sender == "홍길동" && msg == "호출") {
        Phone.call("010-9999-8888");
    }
}
```

#### Target Packages 설정

스크립트 생성 시 **Target Packages**에 감지할 앱 패키지명을 입력하세요:

| 앱 | 패키지명 |
|----|---------|
| 카카오톡 | `com.kakao.talk` |
| 텔레그램 | `org.telegram.messenger` |
| LINE | `jp.naver.line.android` |
| Facebook Messenger | `com.facebook.orca` |
| 전체 (비워두기) | _(빈칸)_ |

---

## 스크립트 API

### replier — 답장

```javascript
replier.reply("메시지");           // 현재 채팅방에 답장
replier.reply(room, "메시지");     // 특정 방에 답장
```

### Phone — 전화

```javascript
Phone.call("010-1234-5678");      // 자동 전화 걸기
Phone.dial("010-1234-5678");      // 다이얼러 열기 (수동)
```

### Sms — 문자

```javascript
Sms.send("010-1234-5678", "내용");  // SMS 전송
```

### Http — HTTP 요청

```javascript
var res = Http.requestSync("https://api.example.com");
replier.reply(res.body);

var res2 = Http.requestSync("https://api.example.com", {
    method: "POST",
    body: JSON.stringify({ key: "value" }),
    headers: { "Content-Type": "application/json" }
});
```

### Log — 로그

```javascript
Log.log("디버그 메시지");          // 로그 출력 (앱 Logs 탭에서 확인)
```

---

## 지원 메신저

| 앱 | 패키지명 | 자동응답 | 알림 필요 |
|----|---------|---------|---------|
| 카카오톡 | `com.kakao.talk` | ✅ | ✅ |
| 텔레그램 | `org.telegram.messenger` | ✅ | ✅ |
| LINE | `jp.naver.line.android` | ✅ | ✅ |
| Facebook Messenger | `com.facebook.orca` | ✅ | ✅ |
| Instagram | `com.instagram.android` | ✅ | ✅ |

> 알림이 꺼져 있으면 메시지를 감지하지 못합니다. 각 앱의 알림을 허용해 주세요.

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| 서버 | Node.js, Express, Socket.IO, MongoDB, Mongoose |
| 웹 | React, Vite, TailwindCSS, Monaco Editor, Zustand |
| 안드로이드 | Kotlin, Jetpack Compose, Hilt, Room, Rhino JS |
| 인증 | JWT (Access Token + Refresh Token + Device Token) |
| 배포 | Docker, Docker Compose |

---

## 문제 해결

| 증상 | 원인 | 해결법 |
|------|------|--------|
| 웹 대시보드 접속 안 됨 | 컨테이너 미실행 | `docker compose ps` 확인 후 `docker compose up -d` |
| `Invalid pairing code` | 코드 만료 | 코드는 10분 유효 — Devices 탭에서 재발급 |
| 봇 응답 없음 | 알림 권한 없음 | Permissions 탭 → 알림 접근 허용 |
| 봇 응답 없음 | Bot Service 꺼짐 | Home 탭 → Bot Service ON |
| 봇 응답 없음 | 스크립트 비활성화 | Scripts 탭 → 스위치 ON |
| 봇 응답 없음 | Target Packages 불일치 | 스크립트 Target Packages에 앱 패키지명 추가 또는 비우기 |
| 앱 종료 후 봇 안 됨 | 배터리 최적화 | 설정 → 배터리 → _nolanbot → 최적화 안 함 |
| Cloud Offline | 서버 URL 오류 | `localhost` 대신 서버 실제 IP 입력 |
| `CLEARTEXT not permitted` | HTTP 차단 | Server URL이 `http://`인지 확인 (앱에서 허용됨) |

### 로그 확인

```bash
# 서버 로그
docker compose logs -f server

# 웹 로그
docker compose logs -f web

# 전체 로그
docker compose logs -f
```

---

---

# English

**[한국어](#_nolanbot)** | English

_nolanbot is a messenger bot platform that automates KakaoTalk, Telegram, LINE, Messenger, and Instagram using **JavaScript scripts**.
Your Android phone acts as the bot engine, while a web dashboard lets you write, deploy, and monitor scripts in real time.

---

## Table of Contents

- [How It Works](#how-it-works)
- [Features](#features)
- [Server Installation](#server-installation-linux--docker)
- [Android App Setup](#android-app-setup)
- [Pairing App with Server](#pairing-app-with-server)
- [Writing Bot Scripts](#writing-bot-scripts)
- [Script API Reference](#script-api-reference)
- [Supported Messengers](#supported-messengers)
- [Tech Stack](#tech-stack)
- [Troubleshooting](#troubleshooting)

---

## How It Works

```
[KakaoTalk / Telegram / LINE / etc.]
          ↓ Notification received
   [Android Phone]
   · Intercepts notifications (NotificationListenerService)
   · Executes JavaScript scripts (Mozilla Rhino engine)
   · Auto-reply / Send SMS / Make calls
          ↕ Socket.IO (real-time bidirectional)
   [Cloud Server (Node.js + MongoDB)]
          ↕
   [Web Dashboard (React)]
   · Script editor (Monaco Editor)
   · Real-time message logs
   · Device management & monitoring
```

1. A notification arrives from a messenger app — the Android app intercepts it
2. Registered JavaScript scripts are executed against the message
3. Scripts can reply, send SMS, make phone calls, or call external APIs
4. All activity is visible in real time on the web dashboard

---

## Features

- ✅ **Auto-reply** — Automatically respond to messages in KakaoTalk, Telegram, and more
- ✅ **Auto-call** — Automatically call a number when a specific message is received
- ✅ **Auto-SMS** — Send text messages from scripts
- ✅ **HTTP API integration** — Call external APIs and use the response
- ✅ **Remote script deployment** — Edit on the web dashboard, instantly applied to your phone
- ✅ **Real-time logs** — Monitor incoming and outgoing messages live
- ✅ **Multi-device management** — Manage multiple Android phones from one dashboard
- ✅ **Runs after app close** — Background foreground service keeps the bot alive

---

## Server Installation (Linux + Docker)

### Prerequisites

- Ubuntu 20.04 or later
- Docker Engine 24.0 or later

### 1. Install Docker

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker
```

### 2. Clone the Repository

```bash
git clone https://github.com/NolanKiwi/megabot.git
cd megabot
```

### 3. Configure Environment Variables

```bash
cp server/.env.example server/.env
nano server/.env
```

Edit the file with the following values:

```env
PORT=3000
MONGODB_URI=mongodb://mongodb:27017/megabot
JWT_ACCESS_SECRET=your-random-secret-key-at-least-32-chars
JWT_REFRESH_SECRET=your-other-random-secret-key-at-least-32-chars
JWT_ACCESS_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d
CORS_ORIGIN=*
```

> ⚠️ Always change the JWT secret keys. Generate secure keys with:
> ```bash
> openssl rand -hex 32
> ```

### 4. Start the Server

```bash
docker compose up -d
```

### 5. Verify

```bash
docker compose ps
# All 3 containers (server, web, mongodb) should show "Up"

curl http://localhost:3000/api/health
# {"status":"ok",...}
```

Web dashboard: **http://YOUR_SERVER_IP:5173**

### 6. Open Firewall Ports

```bash
sudo ufw allow 3000   # API server
sudo ufw allow 5173   # Web dashboard
```

### Updating

```bash
git pull
docker compose up --build -d
```

---

## Android App Setup

### Prerequisites

- Android Studio (latest version)
- Android 8.0 (API 26) or higher device
- USB debugging enabled on your phone

### Build & Install

1. Install [Android Studio](https://developer.android.com/studio)
2. Open the `android/` folder in Android Studio
3. Wait for Gradle sync to complete
4. Connect your phone via USB with USB debugging ON
5. Click the ▶ **Run** button

The app will be built and installed on your phone automatically.

### Grant Required Permissions

After installation, go to the **Permissions tab** in the app and grant all:

| Permission | Purpose |
|------------|---------|
| Notification Access | Intercept messenger messages (**most important**) |
| Phone Calls | Auto-call feature |
| Send/Read SMS | Auto-SMS feature |
| Read Phone State | Check call status |

> ⚠️ **Notification Access** must be granted via Settings → Apps → Special app access → Notification access.

### Disable Battery Optimization (Required)

To keep the bot running in the background:

**Settings → Battery → Battery optimization → _nolanbot → Don't optimize**

For Samsung devices, also:

**Settings → Apps → _nolanbot → Battery → Unrestricted**

---

## Pairing App with Server

### Step 1 — Generate Pairing Code (Web Dashboard)

1. Open **http://YOUR_SERVER_IP:5173**
2. Create an account and log in
3. Go to **Devices tab** → Click **Generate Pairing Code**
4. Copy the 6-digit code (valid for 10 minutes)

### Step 2 — Connect App (Android)

1. Open the _nolanbot app
2. Go to **Settings tab**
3. **Server URL**: Enter `http://YOUR_SERVER_IP:3000`
4. **Pairing Code**: Enter the 6-digit code
5. Tap **Connect to Cloud**
6. Confirm "Connected" status

### Step 3 — Start Bot Service

1. Go to **Home tab** → Toggle **Bot Service** to **ON**
2. Check the status bar for "_nolanbot - Bot is running" notification

> 💡 Once Bot Service is ON, it continues running in the background even after closing the app.

---

## Writing Bot Scripts

### Creating Scripts

**Method 1 — Directly on the app**
- Scripts tab → **+** button → Write code → Save

**Method 2 — Remote deployment via web dashboard**
- http://YOUR_SERVER_IP:5173/scripts → **New** → Write code → **Save & Deploy**
- Instantly deployed to all connected phones

### Script Structure

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    // room       : Chat room name
    // msg        : Received message content
    // sender     : Sender's name
    // isGroupChat: Whether it's a group chat (true/false)
    // replier    : Reply object
    // packageName: App package name (e.g. org.telegram.messenger)

    if (msg == "hello") {
        replier.reply("Hello! I'm a bot 🤖");
    }
}
```

### Script Examples

#### Basic Auto-Reply

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "ping") {
        replier.reply("pong");
    }

    if (msg == "hello") {
        replier.reply("Hello! Bot is running 🤖");
    }
}
```

#### Auto-Call on Specific Message

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "emergency") {
        Phone.call("010-1234-5678");
        replier.reply("Calling now...");
    }
}
```

#### Auto-SMS on Specific Message

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "alert") {
        Sms.send("010-1234-5678", sender + " sent a message: " + msg);
    }
}
```

#### HTTP API Integration

```javascript
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "weather") {
        var res = Http.requestSync("https://wttr.in/Seoul?format=3");
        replier.reply("Current weather: " + res.body);
    }
}
```

#### Setting Target Packages

In the script editor, set **Target Packages** to filter which apps trigger the script:

| App | Package Name |
|-----|-------------|
| KakaoTalk | `com.kakao.talk` |
| Telegram | `org.telegram.messenger` |
| LINE | `jp.naver.line.android` |
| Facebook Messenger | `com.facebook.orca` |
| All apps | _(leave empty)_ |

---

## Script API Reference

### replier — Send replies

```javascript
replier.reply("message");           // Reply in current chat room
replier.reply(room, "message");     // Reply in specific room
```

### Phone — Make calls

```javascript
Phone.call("010-1234-5678");       // Auto-dial (requires permission)
Phone.dial("010-1234-5678");       // Open dialer (manual confirmation)
```

### Sms — Send texts

```javascript
Sms.send("010-1234-5678", "message");
```

### Http — HTTP requests

```javascript
var res = Http.requestSync("https://api.example.com");
replier.reply(res.body);

var res2 = Http.requestSync("https://api.example.com", {
    method: "POST",
    body: JSON.stringify({ key: "value" }),
    headers: { "Content-Type": "application/json" }
});
```

### Log — Logging

```javascript
Log.log("debug message");    // View in app's Logs tab
```

---

## Supported Messengers

| App | Package Name | Auto-reply | Notification Required |
|-----|-------------|------------|----------------------|
| KakaoTalk | `com.kakao.talk` | ✅ | ✅ |
| Telegram | `org.telegram.messenger` | ✅ | ✅ |
| LINE | `jp.naver.line.android` | ✅ | ✅ |
| Facebook Messenger | `com.facebook.orca` | ✅ | ✅ |
| Instagram | `com.instagram.android` | ✅ | ✅ |

> Notifications must be enabled for each app. The bot cannot detect messages if notifications are off.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Server | Node.js, Express, Socket.IO, MongoDB, Mongoose |
| Web | React, Vite, TailwindCSS, Monaco Editor, Zustand |
| Android | Kotlin, Jetpack Compose, Hilt, Room, Rhino JS Engine |
| Auth | JWT (Access Token + Refresh Token + Device Token) |
| Deployment | Docker, Docker Compose |

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Can't access web dashboard | Container not running | `docker compose ps` then `docker compose up -d` |
| `Invalid pairing code` | Code expired | Codes are valid for 10 minutes — regenerate in Devices tab |
| Bot not responding | No notification permission | Permissions tab → Grant Notification Access |
| Bot not responding | Bot Service off | Home tab → Bot Service ON |
| Bot not responding | Script disabled | Scripts tab → Toggle script ON |
| Bot not responding | Target package mismatch | Add app package name to Target Packages or leave empty |
| Bot stops after app close | Battery optimization | Settings → Battery → _nolanbot → Don't optimize |
| Cloud shows Offline | Wrong server URL | Use actual server IP, not `localhost` |

### View Logs

```bash
# Server logs
docker compose logs -f server

# Web logs
docker compose logs -f web

# All logs
docker compose logs -f
```

---

## License

MIT

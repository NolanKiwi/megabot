# MegaBot

카카오톡, 텔레그램, LINE, Messenger, Instagram을 **JavaScript 코드로 자동화**하는 메신저 봇 플랫폼입니다.

안드로이드 폰과 클라우드 서버를 연결해 웹 대시보드에서 봇 스크립트를 작성·배포하고, 실시간으로 메시지를 모니터링합니다.

---

## 아키텍처

```
[카카오톡 / 텔레그램 등]
        ↓ 알림 수신
[안드로이드 앱]  ──Socket.IO──  [서버 (Node.js + MongoDB)]
  · 알림 가로채기                       ↑
  · JS 스크립트 실행 (Rhino)     [웹 대시보드 (React)]
  · 자동 답장 / SMS / 전화         · 스크립트 에디터
                                  · 실시간 로그
                                  · 기기 관리
```

---

## 설치 방법 (Linux + Docker)

### 필수 조건

- Ubuntu 20.04 이상
- Docker & Docker Compose

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

`server/.env` 내용을 아래처럼 수정하세요:

```env
PORT=3000
MONGODB_URI=mongodb://mongodb:27017/megabot
JWT_ACCESS_SECRET=여기에-랜덤-시크릿-키-입력-32자이상
JWT_REFRESH_SECRET=여기에-다른-랜덤-시크릿-키-입력-32자이상
JWT_ACCESS_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d
CORS_ORIGIN=*
```

> ⚠️ JWT 시크릿 키는 반드시 변경하세요. 아래 명령으로 안전한 키를 생성할 수 있습니다:
> ```bash
> openssl rand -hex 32
> ```

### 4. 실행

```bash
docker compose up -d
```

### 5. 확인

```bash
docker compose ps
# 세 컨테이너가 모두 Up 상태여야 합니다

curl http://localhost:3000/api/health
# {"status":"ok",...}
```

웹 대시보드: **http://서버IP:5173**

---

## 방화벽 포트 열기

```bash
# UFW 사용 시
sudo ufw allow 3000   # API 서버
sudo ufw allow 5173   # 웹 대시보드
```

---

## 안드로이드 앱 설치

### 빌드

1. [Android Studio](https://developer.android.com/studio) 설치
2. `android/` 폴더 열기
3. Gradle sync 완료 대기
4. 폰 연결 → USB 디버깅 ON
5. ▶ Run

### 앱 연결 (페어링)

1. **웹 대시보드** → Devices → "Generate Pairing Code" → 6자리 코드 복사
2. **안드로이드 앱** → Settings 탭
   - Server URL: `http://서버IP:3000`
   - Pairing Code: 복사한 6자리 코드
3. "Connect to Cloud" 탭

> 💡 Server URL에는 `localhost`가 아닌 서버의 **실제 IP**를 입력하세요.

---

## 봇 스크립트 예제

웹 대시보드 → Scripts 탭에서 작성합니다.

### 기본 자동 답장

```javascript
Bot.on('message', function(msg) {
    if (msg.content === '안녕') {
        Bot.reply(msg, '안녕하세요! 저는 MegaBot이에요 🤖');
    }
});
```

### 명령어 봇

```javascript
Bot.on('message', function(msg) {
    var text = msg.content;

    if (text === '/도움말') {
        Bot.reply(msg,
            '📋 명령어 목록\n' +
            '/시간 - 현재 시각\n' +
            '/에코 [텍스트] - 따라하기'
        );
    }

    if (text === '/시간') {
        Bot.reply(msg, '현재 시각: ' + new Date().toLocaleString('ko-KR'));
    }

    if (text.startsWith('/에코 ')) {
        Bot.reply(msg, text.replace('/에코 ', ''));
    }
});
```

### 외부 API 연동

```javascript
Bot.on('message', function(msg) {
    if (msg.content === '/날씨') {
        var res = Http.get('https://wttr.in/Seoul?format=3');
        Bot.reply(msg, '🌤 ' + res.body);
    }
});
```

---

## 스크립트 API 목록

| API | 설명 | 예시 |
|-----|------|------|
| `Bot.on('message', fn)` | 메시지 수신 이벤트 | - |
| `Bot.reply(msg, text)` | 메시지 답장 | `Bot.reply(msg, '안녕')` |
| `Http.get(url)` | HTTP GET 요청 | `Http.get('https://...')` |
| `Http.post(url, body)` | HTTP POST 요청 | - |
| `Sms.send(number, text)` | SMS 전송 | `Sms.send('010-...', '내용')` |
| `Phone.call(number)` | 전화 걸기 | `Phone.call('010-...')` |
| `Db.set(key, value)` | 로컬 DB 저장 | `Db.set('count', 1)` |
| `Db.get(key)` | 로컬 DB 조회 | `Db.get('count')` |
| `Log.info(text)` | 로그 출력 | `Log.info('실행됨')` |

---

## 지원 메신저

- 카카오톡 (KakaoTalk)
- 텔레그램 (Telegram)
- LINE
- Facebook Messenger
- Instagram

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| 서버 | Node.js, Express, Socket.IO, MongoDB |
| 웹 | React, Vite, TailwindCSS, Monaco Editor |
| 안드로이드 | Kotlin, Jetpack Compose, Hilt, Room |
| JS 엔진 | Mozilla Rhino |
| 배포 | Docker, Docker Compose, Nginx |

---

## 문제 해결

| 증상 | 해결법 |
|------|--------|
| 웹 접속 안 됨 | `docker compose ps` 로 컨테이너 상태 확인 |
| `Invalid pairing code` | 코드는 10분 유효 — 재발급 후 재시도 |
| 봇 응답 없음 | 앱 Permissions 탭에서 알림 접근 권한 허용 |
| Cloud Offline | Server URL이 서버 실제 IP인지 확인 |

```bash
# 로그 확인
docker compose logs -f server
docker compose logs -f web
```

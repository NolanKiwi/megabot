FROM node:20-alpine AS builder
WORKDIR /app

# 워크스페이스 package.json 먼저 복사 (레이어 캐시 활용)
COPY package.json ./
COPY shared/package.json ./shared/
COPY server/package.json ./server/
COPY web/package.json ./web/

RUN npm install

# 소스 복사 및 빌드
COPY shared/ ./shared/
COPY web/ ./web/
RUN npm run build:shared && npm run build:web

# ── 프로덕션 이미지 (Nginx) ──
FROM nginx:alpine
COPY --from=builder /app/web/dist /usr/share/nginx/html
COPY web/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

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
COPY server/ ./server/
RUN npm run build:shared && npm run build:server

# ── 프로덕션 이미지 ──
FROM node:20-alpine
WORKDIR /app

COPY --from=builder /app/node_modules        ./node_modules
COPY --from=builder /app/package.json        ./
COPY --from=builder /app/shared/dist         ./shared/dist
COPY --from=builder /app/shared/package.json ./shared/
COPY --from=builder /app/server/dist         ./server/dist
COPY --from=builder /app/server/package.json ./server/

WORKDIR /app/server
EXPOSE 3000
CMD ["node", "dist/index.js"]

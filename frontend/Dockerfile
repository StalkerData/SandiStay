# ==========================================
# ETAPA 1: Constructor (Builder)
# ==========================================
FROM node:24-alpine AS builder
WORKDIR /build

RUN corepack enable pnpm
RUN pnpm config set strict-dep-builds false

COPY package.json pnpm-lock.yaml svelte.config.js vite.config.ts ./
RUN pnpm install --frozen-lockfile

COPY . .

# Recibimos la URL por ARG en tiempo de construcción (Build-time)
ARG PUBLIC_API_URL
ENV PUBLIC_API_URL=$PUBLIC_API_URL

RUN pnpm run build
RUN pnpm install --prod --frozen-lockfile --ignore-scripts

# ==========================================
# ETAPA 2: Producción (Runner)
# ==========================================
FROM node:24-alpine
WORKDIR /app

RUN chown -R node:node /app

COPY --from=builder --chown=node:node /build/build ./build
COPY --from=builder --chown=node:node /build/node_modules ./node_modules
COPY --from=builder --chown=node:node /build/package.json ./

USER node

ENV NODE_ENV=production
EXPOSE 3000

CMD ["node", "build/index.js"]

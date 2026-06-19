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

# SOLUCIÓN AL ERROR: Inyectamos la variable estática para que el build no falle.
# Al ser estática, SvelteKit "quemará" este valor en el código compilado.
ENV PUBLIC_API_URL=http://localhost/api/v1

RUN pnpm run build
RUN pnpm install --prod --frozen-lockfile --ignore-scripts

# ==========================================
# ETAPA 2: Producción (Runner)
# ==========================================
FROM node:24-alpine
WORKDIR /app

COPY --from=builder /build/build ./build
COPY --from=builder /build/node_modules ./node_modules
COPY --from=builder /build/package.json ./

EXPOSE 3000
CMD ["node", "build/index.js"]

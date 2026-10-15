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

# Esta variable se usa en tiempo de compilación para SvelteKit
ENV PUBLIC_API_URL=http://localhost/api/v1

RUN pnpm run build
RUN pnpm install --prod --frozen-lockfile --ignore-scripts

# ==========================================
# ETAPA 2: Producción (Runner)
# ==========================================
FROM node:24-alpine
WORKDIR /app

# SECURITY BY DESIGN: Usar el usuario sin privilegios incluido en la imagen
RUN chown -R node:node /app

COPY --from=builder --chown=node:node /build/build ./build
COPY --from=builder --chown=node:node /build/node_modules ./node_modules
COPY --from=builder --chown=node:node /build/package.json ./

# Cambiar al usuario seguro
USER node

ENV NODE_ENV=production
EXPOSE 3000

CMD ["node", "build/index.js"]

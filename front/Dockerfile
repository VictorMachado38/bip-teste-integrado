# ---- Build Stage ----
FROM node:20-alpine AS builder
WORKDIR /build

# Copia package.json e instala dependências em cache
COPY bip/package*.json ./
RUN npm ci --prefer-offline

# Copia o source e faz o build de produção
COPY bip/ ./
ARG BUILD_SCRIPT=build
RUN npm run $BUILD_SCRIPT

# ---- Runtime Stage ----
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

COPY --from=builder /build/dist/bip/browser ./
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
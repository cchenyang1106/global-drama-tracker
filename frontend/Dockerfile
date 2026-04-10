# 第一阶段：构建
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 第二阶段：Nginx 部署
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80

# Nginx 官方 Docker 镜像自动用 envsubst 处理 /etc/nginx/templates/ 下的模板
# 环境变量 BACKEND_URL 将被替换到 nginx 配置中
ENV BACKEND_URL=http://backend:8080

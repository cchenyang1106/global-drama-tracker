# 第一阶段：构建
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 第二阶段：Nginx 部署
FROM nginx:alpine

# 移除默认配置
RUN rm /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Railway 会设置 PORT 环境变量，默认 80
ENV PORT=80
ENV BACKEND_URL=http://backend:8080

# Nginx 需要监听 PORT
CMD sh -c "envsubst '\$PORT \$BACKEND_URL' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"

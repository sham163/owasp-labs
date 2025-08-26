# build React
FROM node:18-alpine AS build
WORKDIR /app

# Copy package manifests (lockfile may or may not exist)
COPY package*.json ./

# If a lockfile exists, use npm ci; otherwise use npm install
RUN if [ -f package-lock.json ]; then npm ci; else npm install --no-audit --no-fund; fi

# Copy the rest and build
COPY . .
RUN npm run build

# serve with nginx and proxy /api
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80

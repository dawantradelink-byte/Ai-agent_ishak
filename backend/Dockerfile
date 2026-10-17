# Production Container for Autonomous AI Team Cloud Backend
FROM node:20-alpine

WORKDIR /app

# Install dependencies
COPY package.json ./
RUN npm install --production

# Copy application code
COPY server.js ./

# Set environment
ENV NODE_ENV=production
ENV PORT=8080
ENV WORKSPACE_DIR=/app/workspace

# Expose port
EXPOSE 8080

# Volume mount for persistent storage
VOLUME ["/app/workspace"]

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

CMD ["node", "server.js"]

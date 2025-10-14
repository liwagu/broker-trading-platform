FROM python:3.12-slim AS base

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    KRONOS_MODE=auto \
    KRONOS_DEVICE=cpu

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt ./

# Optional Kronos installation (set at build time: --build-arg INCLUDE_KRONOS=true)
ARG INCLUDE_KRONOS=false

RUN pip install --upgrade pip \
    && pip install -r requirements.txt \
    && if [ "$INCLUDE_KRONOS" = "true" ]; then pip install kronos; fi

COPY . .

EXPOSE 5001

CMD ["python", "main.py"]

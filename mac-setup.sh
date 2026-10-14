#!/usr/bin/env bash
# One-command setup for the spring-ai-lab on macOS.
# Installs Ollama, JDK 17, Maven, pulls the two models, then verifies everything.
# Usage:  chmod +x mac-setup.sh && ./mac-setup.sh
set -euo pipefail

say() { printf '\n==> %s\n' "$*"; }

# --- Homebrew ---------------------------------------------------------------
if ! command -v brew >/dev/null 2>&1; then
  say "Installing Homebrew (needs your password once)..."
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
  if [[ -x /opt/homebrew/bin/brew ]]; then
    eval "$(/opt/homebrew/bin/brew shellenv)"
  elif [[ -x /usr/local/bin/brew ]]; then
    eval "$(/usr/local/bin/brew shellenv)"
  fi
fi

# --- Ollama -----------------------------------------------------------------
if ! command -v ollama >/dev/null 2>&1; then
  say "Installing Ollama app..."
  brew install --cask ollama
else
  say "Ollama already installed, skipping."
fi

# Launch the app so the server starts (menu-bar icon appears).
open -a Ollama 2>/dev/null || true
for i in $(seq 1 30); do
  if curl -fs -o /dev/null http://localhost:11434 2>/dev/null; then break; fi
  sleep 1
done

# --- Java 17 + Maven ---------------------------------------------------------
if ! brew list openjdk@17 >/dev/null 2>&1; then
  say "Installing JDK 17..."
  brew install openjdk@17
fi
if ! command -v mvn >/dev/null 2>&1; then
  say "Installing Maven..."
  brew install maven
fi

export JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || \
  echo "${HOMEBREW_PREFIX:-/opt/homebrew}/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home")"
export PATH="$JAVA_HOME/bin:$PATH"

# --- Models -----------------------------------------------------------------
say "Pulling models (llama3.1 ~4.7 GB — a few minutes on first run)..."
ollama pull llama3.1
ollama pull nomic-embed-text

# --- Verify -----------------------------------------------------------------
say "Verifying..."
echo "--- ollama ---"
curl -s http://localhost:11434 && echo
ollama list
echo "--- java ---"
java -version 2>&1
echo "--- maven ---"
mvn -v | head -3

say "All set. To run the lab (in a terminal inside the spring-ai-lab folder):"
echo "    mvn spring-boot:run"
echo "Then open http://localhost:8080"

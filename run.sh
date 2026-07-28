#!/usr/bin/env bash
set -e
cd /home/owen/Baseless

cd frontend && npm run build && npm run deploy &
cd /home/owen/Baseless


exec kotlin -cp app.jar com.ownwn.baseless.BaselessApplicationKt

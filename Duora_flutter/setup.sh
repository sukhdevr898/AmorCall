#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "🚀 DUORA FLUTTER AUTOMATED BUILD & SETUP SCRIPT"
echo "=========================================================="

# 1. System Dependencies Check
echo "📦 Step 1: Checking required system tools..."
for tool in git curl unzip; do
  if ! command -v "$tool" &> /dev/null; then
    echo "Installing missing dependency: $tool..."
    if command -v sudo &> /dev/null; then
      sudo apt-get update && sudo apt-get install -y "$tool"
    else
      apt-get update && apt-get install -y "$tool"
    fi
  fi
done

# 2. Flutter SDK Installation
echo "📱 Step 2: Checking Flutter SDK installation..."
export FLUTTER_HOME="$HOME/flutter"
export PATH="$FLUTTER_HOME/bin:$PATH"

if ! command -v flutter &> /dev/null; then
  if [ -d "$FLUTTER_HOME" ]; then
    echo "Found existing Flutter directory at $FLUTTER_HOME, adding to PATH..."
  else
    echo "Cloning Flutter stable SDK..."
    git clone https://github.com/flutter/flutter.git -b stable --depth 1 "$FLUTTER_HOME"
  fi
  export PATH="$FLUTTER_HOME/bin:$PATH"
fi

if ! grep -q "flutter/bin" "$HOME/.bashrc" 2>/dev/null; then
  echo 'export PATH="$HOME/flutter/bin:$PATH"' >> "$HOME/.bashrc"
fi

echo "Flutter version:"
flutter --version

# 3. Android Licenses
echo "📝 Step 3: Checking Android licenses..."
yes | flutter doctor --android-licenses 2>/dev/null || true

# 4. Clone or Verify AmorCall Repository
echo "📂 Step 4: Setting up AmorCall repository..."
REPO_DIR="$HOME/AmorCall"
if [ ! -d "$REPO_DIR" ]; then
  echo "Cloning https://github.com/sukhdevr898/AmorCall into $REPO_DIR..."
  git clone https://github.com/sukhdevr898/AmorCall.git "$REPO_DIR"
fi

cd "$REPO_DIR"

if [ ! -d "Duora_flutter" ]; then
  echo "Error: Duora_flutter directory not found in $REPO_DIR!"
  exit 1
fi

cd Duora_flutter

# 5. Fix Android v1 Embedding & Scaffolding
echo "🛠️ Step 5: Regenerating clean Android v2 embedding scaffolding..."
rm -rf android
flutter create . --platforms=android --org com.aistudio.amourcall.kpqlzx

if [ -f "android/app/build.gradle" ]; then
  sed -i 's/minSdkVersion flutter.minSdkVersion/minSdkVersion 21/g' android/app/build.gradle 2>/dev/null || true
  sed -i 's/minSdk = flutter.minSdkVersion/minSdk = 21/g' android/app/build.gradle 2>/dev/null || true
fi

# 6. Create .env Directly (No cp needed)
echo "🔒 Step 6: Creating .env configuration directly..."
cat << 'EOF' > .env
SERVER_URL=https://amorcall.onrender.com
WEBSOCKET_URL=wss://amorcall.onrender.com
FIREBASE_API_KEY=AIzaSyBP4sQvCmbzd31mmG-clgJTCRX5_q9vODg
FIREBASE_PROJECT_ID=gen-lang-client-0617344108
FIREBASE_MESSAGING_SENDER_ID=531599159732
FIREBASE_APP_ID=1:531599159732:android:8ff660c6233aadff08c2e1
STUN_SERVER_1=stun:stun.l.google.com:19302
STUN_SERVER_2=stun:stun1.l.google.com:19302
EOF

# 7. Create google-services.json Directly (No cp needed)
echo "🔥 Step 7: Creating google-services.json directly..."
mkdir -p android/app
cat << 'EOF' > android/app/google-services.json
{
  "project_info": {
    "project_number": "531599159732",
    "project_id": "gen-lang-client-0617344108",
    "storage_bucket": "gen-lang-client-0617344108.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:531599159732:android:8ff660c6233aadff08c2e1",
        "android_client_info": {
          "package_name": "com.aistudio.amourcall.kpqlzx"
        }
      },
      "oauth_client": [
        {
          "client_id": "531599159732-0qvmv1mls6l0nti60nfom7vjd8m9n0t6.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.aistudio.amourcall.kpqlzx",
            "certificate_hash": "b49a61e466820f983f1c73bc12a1c02c143c310d"
          }
        },
        {
          "client_id": "531599159732-jeep919lqmr0nc7i5f7e9oo6vke8edlq.apps.googleusercontent.com",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "AIzaSyBP4sQvCmbzd31mmG-clgJTCRX5_q9vODg"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": [
            {
              "client_id": "531599159732-jeep919lqmr0nc7i5f7e9oo6vke8edlq.apps.googleusercontent.com",
              "client_type": 3
            }
          ]
        }
      }
    }
  ],
  "configuration_version": "1"
}
EOF

cp android/app/google-services.json google-services.json

# 8. Clean and Get Dependencies
echo "📦 Step 8: Installing Flutter dependencies..."
flutter clean
flutter pub get

# 9. Build Release APK
echo "⚙️ Step 9: Compiling Release APK..."
flutter build apk --release

echo ""
echo "=========================================================="
echo "🎉 BUILD SUCCESSFUL!"
echo "=========================================================="
echo "Your Release APK is ready at:"
echo "👉 $(pwd)/build/app/outputs/flutter-apk/app-release.apk"
echo "=========================================================="

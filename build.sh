set -euo pipefail

mkdir -p target/classes

if ! find src/broker -name '*.java' -print0 \
    | xargs -0 javac -d target/classes -sourcepath src/main/java; then
    echo "Build failed" >&2
    exit 1
fi

echo "Build successful"
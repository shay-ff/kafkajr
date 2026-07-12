set -euo pipefail

if [ ! -d target/classes ]; then
    echo "target/classes not found — building first..."
    bash build.sh
fi

mkdir -p data/kafka-jr

java -cp target/classes broker.kafkajrBroker \
    --port "${1:-8080}" \
    --data-dir data/kafka-jr
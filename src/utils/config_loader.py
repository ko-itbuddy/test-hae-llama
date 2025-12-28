import yaml
import os

class EngineConfig:
    _instance = None
    _config = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(EngineConfig, cls).__new__(cls)
            cls._instance._load_config()
        return cls._instance

    def _load_config(self):
        # 💡 Defaults
        self._config = {
            "llm": {"model": "qwen2.5-coder:14b", "temperature": 0.3},
            "embeddings": {"model": "nomic-embed-text"},
            "paths": {"data_root": ".test-hea-llama"}
        }
        
        config_path = os.path.join(".test-hea-llama", "config", "engine_config.yml")
        if os.path.exists(config_path):
            try:
                with open(config_path, "r", encoding="utf-8") as f:
                    user_config = yaml.safe_load(f)
                    if user_config:
                        # Deep merge or update
                        self._config.update(user_config)
            except Exception as e:
                print(f"⚠️ Failed to load engine config: {e}")

    def get(self, key, default=None):
        parts = key.split(".")
        val = self._config
        for p in parts:
            if isinstance(val, dict) and p in val:
                val = val[p]
            else:
                return default
        return val

# Global instance
config = EngineConfig()

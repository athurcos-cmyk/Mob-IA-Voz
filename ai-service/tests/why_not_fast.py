"""Diagnostico: para as frases em que a IA acha a analise vazia mas o atalho NAO dispensa, diz qual pista bloqueou. Uso: python tests/why_not_fast.py [categoria]"""
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from app import fastpath  # noqa: E402
from tests.battery_fastpath import GEN_FILE, JUDGED_FILE, empty_analysis  # noqa: E402

judged = json.loads(JUDGED_FILE.read_text(encoding="utf-8"))
gen = json.loads(GEN_FILE.read_text(encoding="utf-8"))
want = sys.argv[1:] or list(gen)
reasons = {}
for cat in want:
    for t in gen.get(cat, []):
        if t in judged and empty_analysis(judged[t]) and not fastpath.is_simple(t):
            n = fastpath._THAT_HAS.sub("o que", fastpath._norm(t))
            m = fastpath._CUES.search(n)
            words = re.findall(r"[a-z]+", n)
            why = ("pista:" + m.group(0)) if m else ("item:" + next((w for w in words if w in fastpath._ITEMS or w.rstrip("s") in fastpath._ITEMS), "") if any(w in fastpath._ITEMS or w.rstrip("s") in fastpath._ITEMS for w in words) else "nao e pergunta/cumprimento")
            reasons.setdefault(why, []).append(f"[{cat}] {t}")
for why, lst in sorted(reasons.items(), key=lambda kv: -len(kv[1])):
    print(f"{len(lst):3d}  {why}")
    for x in lst[:3]:
        print("       ", x)

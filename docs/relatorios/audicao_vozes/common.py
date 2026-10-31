import ctypes
import ctypes.wintypes as wt
import json
import time
from pathlib import Path

OUT = Path(__file__).resolve().parent / "wav"
OUT.mkdir(exist_ok=True)

# (id, rotulo, criatura do mod, frase)
LINES = [
    ("1_vaca", "vaca, calma", "minecraft:cow", "Muuu, tudo bem por aqui. O pasto está macio hoje e o vento ajuda a correr."),
    ("2_galinha", "galinha, aguda", "minecraft:chicken", "Có có! Achei umas sementes no chão. Você trouxe mais pra mim?"),
    ("3_zumbi", "zumbi, grave e bravo", "minecraft:zombie", "Você quer me machucar? Então vem. A fome é maior que o medo."),
    ("4_warden", "warden, grave e baixo", "minecraft:warden", "Fale baixo, por favor. Cada palavra sua ecoa fundo nos meus ossos."),
    ("5_aldeao", "aldeão, conversa de vila", "minecraft:villager#farmer", "A plantação de trigo está linda este ano. Se sobrar, a gente troca."),
    ("6_porco", "porco, falando seu nome", "minecraft:pig", "Bia, né? Guardei. Prazer é meu, e se tiver uma cenoura no bolso, o prazer é maior."),
]

# Kokoro nao muda o tom: escolhe a voz e a velocidade que mais se parecem com o jeito da criatura
KOKORO = {
    "minecraft:cow": ("pf_dora", 0.95), "minecraft:chicken": ("pf_dora", 1.15), "minecraft:zombie": ("pm_santa", 0.85),
    "minecraft:warden": ("pm_santa", 0.8), "minecraft:villager#farmer": ("pm_alex", 1.0), "minecraft:pig": ("pm_alex", 1.05),
}


class PMC(ctypes.Structure):
    _fields_ = [("cb", wt.DWORD), ("PageFaultCount", wt.DWORD), ("PeakWorkingSetSize", ctypes.c_size_t), ("WorkingSetSize", ctypes.c_size_t),
                ("QuotaPeakPagedPoolUsage", ctypes.c_size_t), ("QuotaPagedPoolUsage", ctypes.c_size_t),
                ("QuotaPeakNonPagedPoolUsage", ctypes.c_size_t), ("QuotaNonPagedPoolUsage", ctypes.c_size_t),
                ("PagefileUsage", ctypes.c_size_t), ("PeakPagefileUsage", ctypes.c_size_t)]


def ram_mb() -> float:
    """Memoria (working set) deste processo, em MB."""
    k32 = ctypes.WinDLL("kernel32")
    k32.GetCurrentProcess.restype = wt.HANDLE
    ps = ctypes.WinDLL("psapi")
    ps.GetProcessMemoryInfo.argtypes = [wt.HANDLE, ctypes.POINTER(PMC), wt.DWORD]
    p = PMC()
    p.cb = ctypes.sizeof(PMC)
    ps.GetProcessMemoryInfo(k32.GetCurrentProcess(), ctypes.byref(p), p.cb)
    return p.WorkingSetSize / 1e6


def profile(key: str) -> dict:
    root = Path(__file__).resolve().parents[8] if False else Path(r"C:\Users\Thurcos\Desktop\IA Mobs Voice\MobVozIA_2.1")
    data = json.loads((root / "mod-fabric/src/main/resources/mobvoz/profiles.json").read_text(encoding="utf-8"))["profiles"]
    base = key.split("#")[0]
    for p in data:
        if p["id"] == key:
            return p
    for p in data:
        if p["id"] == base:
            return p
    if key == "minecraft:warden":  # sem perfil proprio no mod: usa o derivado; aqui, uma voz grave e lenta
        return {"voice": "pt_BR-jeff-medium", "pitch": 0.7, "speed": 0.85}
    raise KeyError(key)


def timed(fn):
    t = time.perf_counter()
    r = fn()
    return r, time.perf_counter() - t

import unittest
from pathlib import Path

import numpy as np

from app import config, speech


class SpeechUnitTests(unittest.TestCase):
    def test_downsample_ratio(self):
        pcm = (np.zeros(4800, dtype="<i2")).tobytes()
        self.assertEqual(len(speech.pcm48_to_16k(pcm)), 1600)

    def test_resample_length(self):
        out = speech.resample(np.arange(22050, dtype=np.float32), 22050, 48000)
        self.assertEqual(len(out), 48000)


class CleanForSpeechTests(unittest.TestCase):
    """O leitor de voz SOLETRA sons esticados (zzz vira 'ex-ex-ex-e', sss vira 'S-U-S-U'): so o que e falado muda, a legenda fica."""

    def test_buzz_becomes_a_word_and_hiss_disappears(self):
        self.assertEqual("Zum, as flores estão lindas.", speech.clean_for_speech("Zzzz, as flores estão lindas."))
        self.assertEqual("Não entendi. Repete?", speech.clean_for_speech("Ssss... não entendi. Repete?"))
        self.assertEqual("Tem gente por perto.", speech.clean_for_speech("Grrr... tem gente por perto."))
        self.assertEqual("Hmm, não entendi.", speech.clean_for_speech("Hmmm, não entendi."))
        self.assertEqual("Silêncio.", speech.clean_for_speech("Shhh, silêncio."))

    def test_stretched_letters_inside_words_shrink(self):
        self.assertEqual("Hmm, só não entendi.", speech.clean_for_speech("Hmm, sssó não entendi."))

    def test_normal_speech_is_untouched(self):
        for t in ("Hmm, não entendi.", "Pão com ovo e arroz.", "Bem, mas e o mundo?", "Cobre e cobalto, uma coisa só."):
            self.assertEqual(t, speech.clean_for_speech(t), t)

    def test_animal_onomatopoeia_is_not_spoken(self):
        self.assertEqual("Eu só quero pastar em paz.", speech.clean_for_speech("Muuu, eu só quero pastar em paz."))
        self.assertEqual("Será que tem grama?", speech.clean_for_speech("Béééh... será que tem grama?"))
        self.assertEqual("Tem semente?", speech.clean_for_speech("Có! Tem semente?"))
        self.assertEqual("Bom dia.", speech.clean_for_speech("Cocoricó! Bom dia."))

    def test_only_a_sound_becomes_a_short_hmm_not_silence(self):
        self.assertEqual("Hmm.", speech.clean_for_speech("Grrr..."))
        self.assertEqual("Hmm.", speech.clean_for_speech("Sss."))


class TtsEngineTests(unittest.TestCase):
    def test_kokoro_voice_mapping_and_limited_pitch_shift(self):
        self.assertEqual("pm_santa", speech.kokoro_voice("pt_BR-jeff-medium", 0.6)[0])
        self.assertEqual("pf_dora", speech.kokoro_voice("pt_BR-faber-medium", 1.4)[0])
        self.assertEqual("pm_alex", speech.kokoro_voice("pt_BR-cadu-medium", 0.95)[0])
        self.assertEqual("pf_dora", speech.kokoro_voice("pt_BR-faber-medium", 0.95)[0])
        for p in (0.5, 0.7, 1.0, 1.4, 1.6):
            self.assertTrue(0.92 <= speech.kokoro_voice("pt_BR-jeff-medium", p)[1] <= 1.08)

    def test_piper_is_the_light_mode_and_missing_kokoro_falls_back(self):
        import tempfile
        from app import config
        old, was = config.MODELS_DIR, speech._warned
        try:
            config.MODELS_DIR = tempfile.mkdtemp()  # sem os modelos do Kokoro
            self.assertEqual("piper", speech.active_engine("kokoro"))
            self.assertEqual("piper", speech.active_engine())
            self.assertEqual("piper", speech.active_engine("piper"))
        finally:
            config.MODELS_DIR, speech._warned = old, was

    @unittest.skipUnless(speech.kokoro_available() and (Path(config.MODELS_DIR) / "pt_BR-faber-medium.onnx").exists(), "modelos nao instalados")
    def test_pitch_changes_the_voice_not_the_duration(self):
        text = "Tudo bem por aqui, o pasto est\u00e1 macio hoje e o vento ajuda a correr."
        for engine in ("kokoro", "piper"):
            low = len(speech.synthesize(text, "pt_BR-jeff-medium", 0.7, 1.0, engine))
            high = len(speech.synthesize(text, "pt_BR-jeff-medium", 1.4, 1.0, engine))
            self.assertLess(abs(low - high) / max(low, high), 0.2, engine)


if __name__ == "__main__":
    unittest.main()

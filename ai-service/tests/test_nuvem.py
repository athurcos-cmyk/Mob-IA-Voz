import json
import os
import tempfile
import unittest
from unittest import mock

from app import nuvem, speech, vozes


class NuvemTests(unittest.TestCase):
    def setUp(self):
        self.home = tempfile.TemporaryDirectory()
        env = {"MOBVOZ_HOME": self.home.name, "MOBVOZ_AZURE_KEY": "k" * 32, "MOBVOZ_AZURE_REGION": "brazilsouth"}
        self.env = mock.patch.dict(os.environ, env)
        self.env.start()
        os.environ.pop("MOBVOZ_NO_CLOUD", None)
        nuvem._available.clear()
        nuvem._warned.clear()
        nuvem._down_until, nuvem._fails = 0.0, 0
        self.names = {"F": ["pt-BR-AaNeural", "pt-BR-BbNeural"], "M": ["pt-BR-CcNeural", "pt-BR-DdNeural"]}

    def tearDown(self):
        self.env.stop()
        self.home.cleanup()

    def test_no_cloud_switch_and_missing_key_disable_it(self):
        self.assertTrue(nuvem.enabled())
        with mock.patch.dict(os.environ, {"MOBVOZ_NO_CLOUD": "1"}):
            self.assertFalse(nuvem.enabled(), "trava dura das baterias")
            with mock.patch.object(nuvem, "_request") as req:
                self.assertIsNone(nuvem.synthesize("Oi", vozes.plan("cow", 1)))
                req.assert_not_called()
        with mock.patch.dict(os.environ, {"MOBVOZ_AZURE_KEY": ""}):
            self.assertFalse(nuvem.enabled())

    def test_key_file_with_bom_is_read(self):
        os.environ["MOBVOZ_AZURE_KEY"] = ""
        with open(os.path.join(self.home.name, "azure.json"), "w", encoding="utf-8-sig") as f:
            json.dump({"key": "abc", "region": "East US"}, f)
        self.assertEqual(("abc", "eastus"), nuvem.credentials())

    def test_same_creature_same_voice_and_individuals_spread(self):
        a = nuvem.choose(vozes.plan("cow", 5), "minecraft:cow", 5, self.names)
        self.assertEqual(a, nuvem.choose(vozes.plan("cow", 5), "minecraft:cow", 5, self.names))
        picked = {nuvem.choose(vozes.plan("cow", s), "minecraft:cow", s, self.names)[0] for s in range(64)}
        self.assertEqual({"pt-BR-AaNeural", "pt-BR-BbNeural"}, picked, "vaca e voz feminina; os individuos se espalham pelas vozes")
        self.assertIn(nuvem.choose(vozes.plan("zombie", 1), "minecraft:zombie", 1, self.names)[0], self.names["M"])
        self.assertLess(nuvem.choose(vozes.plan("warden", 1), "minecraft:warden", 1, self.names)[1], -4, "warden e bem mais grave")

    def test_ssml_escapes_text_and_limits_prosody(self):
        x = nuvem.ssml("a < b & c", "pt-BR-AaNeural", -3.04, 1.1)
        self.assertIn("a &lt; b &amp; c", x)
        self.assertIn("pitch='-3.0st'", x)
        self.assertIn("rate='+10%'", x)

    def test_daily_limit_stops_spending_and_falls_back(self):
        with mock.patch.object(nuvem, "DAILY_CHARS", 30), mock.patch.object(nuvem, "voices", return_value=self.names), \
                mock.patch.object(nuvem, "_request", return_value=b"\x01\x00" * 500) as req:
            self.assertIsNotNone(nuvem.synthesize("x" * 20, vozes.plan("cow", 1)))
            self.assertIsNone(nuvem.synthesize("y" * 20, vozes.plan("cow", 1)), "passou do limite do dia")
            self.assertEqual(1, req.call_count)

    def test_failures_return_none_and_pause_the_cloud(self):
        with mock.patch.object(nuvem, "voices", return_value=self.names), mock.patch.object(nuvem, "_request", side_effect=OSError("sem rede")) as req:
            for _ in range(3):
                self.assertIsNone(nuvem.synthesize("Oi", vozes.plan("cow", 1)))
            self.assertFalse(nuvem.enabled(), "depois de 3 falhas espera um tempo em vez de travar cada fala")
            self.assertEqual(3, req.call_count)

    def test_speech_uses_cloud_audio_and_falls_back_when_it_says_none(self):
        pcm24 = (b"\x10\x00" * 2400)
        with mock.patch.object(nuvem, "synthesize", return_value=pcm24):
            out = speech.synthesize("Oi", "pt_BR-faber-medium", 1.0, 1.0, "nuvem", "minecraft:cow", 1, "")
        self.assertEqual(4800 * 2, len(out), "24 kHz virou 48 kHz")
        with mock.patch.object(nuvem, "synthesize", return_value=None), mock.patch.object(speech, "_synthesize_piper", return_value=b"piper") as p, \
                mock.patch.object(speech, "kokoro_available", return_value=False):
            self.assertEqual(b"piper", speech.synthesize("Oi", "pt_BR-faber-medium", 1.0, 1.0, "nuvem", "minecraft:cow", 1, ""))
            p.assert_called_once()

    def test_active_engine_nuvem_needs_key(self):
        self.assertEqual("nuvem", speech.active_engine("nuvem"))
        with mock.patch.dict(os.environ, {"MOBVOZ_NO_CLOUD": "1"}):
            self.assertNotEqual("nuvem", speech.active_engine("nuvem"))


if __name__ == "__main__":
    unittest.main()

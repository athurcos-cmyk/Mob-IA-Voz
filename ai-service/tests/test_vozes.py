import unittest

from app import speech, vozes


class VozesTests(unittest.TestCase):
    def test_same_creature_always_has_the_same_voice(self):
        self.assertEqual(vozes.plan("minecraft:cow", 7), vozes.plan("minecraft:cow", 7))
        self.assertEqual(vozes.plan("minecraft:cow", 7), vozes.plan("minecraft:cow", 7 + vozes.BUCKETS), "o individuo e o resto da divisao por 64")

    def test_individuals_of_a_species_sound_different(self):
        plans = {vozes.plan("minecraft:cow", s) for s in range(vozes.BUCKETS)}
        self.assertGreaterEqual(len(plans), 60, "quase cada individuo tem uma voz so dele")
        pitches = [p.pitch for p in plans]
        self.assertGreater(max(pitches) - min(pitches), 0.08)

    def test_species_keep_the_approved_character(self):
        golem = vozes.plan("minecraft:iron_golem", 3)
        self.assertGreater(dict(golem.mix)["pm_santa"], 0.6, "o golem e grave")
        self.assertTrue(0.66 <= golem.pitch <= 0.82, golem.pitch)
        chicken = vozes.plan("minecraft:chicken", 3)
        self.assertLess(chicken.pitch, 1.25, "a galinha nao e mais estridente")
        self.assertEqual("etereo", vozes.plan("minecraft:vex", 1).fx)
        self.assertEqual("", vozes.plan("minecraft:warden", 1).fx, "sem efeito nos demais")

    def test_mixes_are_normalized_and_bounded(self):
        for sp in ("minecraft:cow", "minecraft:villager#farmer", "minecraft:villager#nitwit", "minecraft:tadpole", "minecraft:ender_dragon", "othermod:blob"):
            for seed in (0, 5, 63, 9999):
                p = vozes.plan(sp, seed, "alegre", 1.0, "pt_BR-faber-medium")
                self.assertAlmostEqual(1.0, sum(w for _, w in p.mix), places=6)
                self.assertTrue(all(v in vozes.BASES for v, _ in p.mix))
                self.assertTrue(0.5 <= p.pitch <= 1.6 and 0.7 <= p.speed <= 1.3, (sp, p))

    def test_unlisted_species_differ_from_each_other(self):
        a = vozes.plan("minecraft:tadpole", 1, "", 1.0, "pt_BR-faber-medium")
        b = vozes.plan("minecraft:frog", 1, "", 1.0, "pt_BR-faber-medium")
        self.assertNotEqual(a.mix, b.mix, "especies sem mistura propria nao soam todas iguais")

    def test_villagers_with_different_jobs_and_individuals_differ(self):
        self.assertNotEqual(vozes.plan("minecraft:villager#farmer", 1).mix, vozes.plan("minecraft:villager#librarian", 1).mix)
        self.assertNotEqual(vozes.plan("minecraft:villager#farmer", 1).mix, vozes.plan("minecraft:villager#farmer", 2).mix)

    def test_emotion_changes_pitch_and_speed_a_little(self):
        neutro = vozes.plan("minecraft:cow", 4, "neutro")
        triste = vozes.plan("minecraft:cow", 4, "triste")
        alegre = vozes.plan("minecraft:cow", 4, "alegre")
        self.assertLess(triste.pitch, neutro.pitch)
        self.assertLess(triste.speed, neutro.speed)
        self.assertGreater(alegre.speed, neutro.speed)
        self.assertEqual(neutro, vozes.plan("minecraft:cow", 4, "emocao-que-nao-existe"))

    @unittest.skipUnless(speech.kokoro_available(), "Kokoro nao instalado")
    def test_synthesis_with_species_makes_different_audio_per_individual(self):
        a = speech.synthesize("Oi, tudo bem?", "pt_BR-faber-medium", 1.0, 1.0, "kokoro", "minecraft:cow", 1, "")
        b = speech.synthesize("Oi, tudo bem?", "pt_BR-faber-medium", 1.0, 1.0, "kokoro", "minecraft:cow", 2, "")
        self.assertGreater(len(a), 20000)
        self.assertNotEqual(a, b)


if __name__ == "__main__":
    unittest.main()

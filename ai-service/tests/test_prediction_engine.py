import importlib
import os
import sys
import unittest

import numpy as np


def _reload_module(mode: str = "simulated"):
    os.environ["KRONOS_MODE"] = mode
    sys.modules.pop("prediction_engine", None)
    return importlib.import_module("prediction_engine")


class PredictionEngineTests(unittest.TestCase):
    def test_simulated_prediction_has_expected_shape(self):
        module = _reload_module("simulated")
        np.random.seed(7)

        result = module.generate_prediction("US67066G1040", horizon_days=3)

        self.assertEqual(result.isin, "US67066G1040")
        self.assertEqual(result.security_name, "NVIDIA Corp")
        self.assertEqual(len(result.predictions), 3)
        self.assertIn(result.signal, {"BUY", "SELL", "HOLD"})

        first_point = result.predictions[0]
        self.assertTrue(first_point.date)
        self.assertLessEqual(first_point.confidence_lower, first_point.predicted_price)
        self.assertGreaterEqual(first_point.confidence_upper, first_point.predicted_price)

    def test_simulated_prediction_rejects_unknown_isin(self):
        module = _reload_module("simulated")

        with self.assertRaises(ValueError) as exc:
            module.generate_prediction("UNKNOWN", horizon_days=2)

        self.assertIn("Unknown ISIN", str(exc.exception))

    def test_generate_prediction_rejects_invalid_horizon(self):
        module = _reload_module("simulated")

        with self.assertRaises(ValueError):
            module.generate_prediction("US67066G1040", horizon_days=0)


if __name__ == "__main__":  # pragma: no cover - manual execution helper
    unittest.main()

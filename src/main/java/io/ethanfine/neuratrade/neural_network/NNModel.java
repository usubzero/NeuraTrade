package io.ethanfine.neuratrade.neural_network;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarAction;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NNModel {

    // TODO: doc
    public Model model;
    public Predictor<double[], BarAction> predictor;

    private CBProduct product;
    private CBTimeGranularity timeGranularity;

    public NNModel(CBProduct product, CBTimeGranularity timeGranularity) {
        this.product = product;
        this.timeGranularity = timeGranularity;
        String modelName = product.productName + "," + timeGranularity.seconds + ",NN_MODEL";
        initModelWithName(modelName);
    }

    public NNModel(CBProduct product, CBTimeGranularity timeGranularity, String modelName) {
        this.product = product;
        this.timeGranularity = timeGranularity;
        initModelWithName(modelName);
    }

    private void initModelWithName(String modelName) {
        try {
            Path modelDir = Paths.get("neural_network/models/");
            model = Model.newInstance(modelName);
            model.load(modelDir);

            predictor = model.newPredictor(generatePredictorTranslator());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private Translator<double[], BarAction> generatePredictorTranslator() {
        return new Translator<double[], BarAction>(){
            @Override
            public NDList processInput(TranslatorContext ctx, double[] input) {
                NDManager manager = ctx.getNDManager();
                float[] inputAsFloats = new float[input.length];
                for (int i = 0 ; i < input.length; i++)
                    inputAsFloats[i] = (float) input[i];
                // NOTE: can manually test inputs here, substituting inputAsFloats for an appropriate input.
                NDArray array = manager.create(inputAsFloats);
                return new NDList(array);
            }

            @Override
            public BarAction processOutput(TranslatorContext ctx, NDList list) {
                NDArray temp_arr = list.get(0);
                int argMaxI = temp_arr.logSoftmax(0).argMax().toArray()[0].intValue(); // TODO: find alternative
                return BarAction.values()[argMaxI];
            }

            @Override
            public Batchifier getBatchifier() {
                return Batchifier.STACK;
            }
        };
    }

    public CBProduct getProduct() {
        return this.product;
    }

    public CBTimeGranularity getTimeGranularity() {
        return this.getTimeGranularity();
    }

    /**
     * Generate a prediction according to the model's predictor.
     * @param input The input to generate a prediction on.
     * @return BarAction representing the action that should be taken given the inputs.
     * @throws Exception if the predictor has not yet been instantiated or there was a translation error in predicting.
     */
    public BarAction predict(double[] input) throws Exception {
        if (predictor == null)
            throw new Exception("Predictor for model not instantiated.");
        return predictor.predict(input);
    }

}

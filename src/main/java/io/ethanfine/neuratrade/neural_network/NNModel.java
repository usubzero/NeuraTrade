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
import io.ethanfine.neuratrade.data.models.BarAction;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NNModel {

    static Model model;
    static Predictor<double[], BarAction> predictor;

    static {
        try {
            Path modelDir = Paths.get("neural_network/models/");
            model = Model.newInstance("model");
            model.load(modelDir);

            Translator<double[], BarAction> translator = new Translator<double[], BarAction>(){
                @Override
                public NDList processInput(TranslatorContext ctx, double[] input) {
                    NDManager manager = ctx.getNDManager();
                    float[] inputAsFloats = new float[input.length];
                    for (int i = 0 ; i < input.length; i++)
                        inputAsFloats[i] = (float) input[i];
//                    new float[] {53.0f, -2.f, -0.2f, 0.1f, 0.1f, 4000, 26.31f, 0.11f}
                    NDArray array = manager.create(inputAsFloats);
                    return new NDList(array);
                }

                @Override
                public BarAction processOutput(TranslatorContext ctx, NDList list) {
                    NDArray temp_arr = list.get(0);
                    System.out.println("TEMP:" + temp_arr + "\n");
                    int argMaxI = temp_arr.logSoftmax(0).argMax().toArray()[0].intValue(); // TODO: find alternative, .getInt() on argmax() results in error
                    return BarAction.values()[argMaxI];
                }

                @Override
                public Batchifier getBatchifier() {
                    return Batchifier.STACK;
                }
            };

            predictor = model.newPredictor(translator);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static BarAction predict(double[] input) throws TranslateException {
        return predictor.predict(input);
    }

}

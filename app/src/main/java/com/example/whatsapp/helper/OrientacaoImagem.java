package com.example.whatsapp.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

public class OrientacaoImagem {

    public static Bitmap carrega(Uri caminhoFoto, Context context) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(caminhoFoto);
        ExifInterface exif = new ExifInterface(inputStream);
        String orientacao = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        assert orientacao != null;
        int codigoOrientacao = Integer.parseInt(orientacao);
        Bitmap resultado = null;

        switch (codigoOrientacao) {
            case ExifInterface.ORIENTATION_NORMAL:
                resultado = abreFotoERotaciona(caminhoFoto, 0,context);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                resultado = abreFotoERotaciona(caminhoFoto, 90,context);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                resultado = abreFotoERotaciona(caminhoFoto, 180,context);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                resultado = abreFotoERotaciona(caminhoFoto, 270,context);
                break;
            default:
                Bitmap getMidaBitmap = MediaStore.Images.Media
                        .getBitmap(context.getContentResolver(), caminhoFoto);
                resultado = Bitmap.createScaledBitmap(getMidaBitmap,500,900,
                        true);
        }
        return resultado;
    }

    private static Bitmap abreFotoERotaciona(Uri caminhoFoto, int angulo, Context context) {

        // Abre o bitmap a partir do caminho da foto Bitmap
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), caminhoFoto);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Prepara a operação de rotação com o ângulo escolhido
        Matrix matrix = new Matrix();
        matrix.postRotate(angulo);

        // Cria um novo bitmap a partir do original já com a rotação aplicada
        Bitmap bitmapFormatado=  Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        return Bitmap.createScaledBitmap(bitmapFormatado,500,900,
                true);
    }
}

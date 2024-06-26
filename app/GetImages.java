import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class GetImages extends AsyncTask<Object, Object, Object> {
    private String requestUrl, imagename_;
    private ImageView view;
    private Bitmap bitmap;
    private FileOutputStream fos;
    private GetImages(String requestUrl, ImageView view, String _imagename_) {
        this.requestUrl = requestUrl;
        this.view = view;
        this.imagename_ = _imagename_;
    }
    @Override
    protected Object doInBackground(Object... objects) {
        try {
            URL url = new URL(requestUrl);
            URLConnection conn = url.openConnection();
            bitmap = BitmapFactory.decodeStream(conn.getInputStream());
        } catch (Exception ex) {
        }
        return null;
    }
}

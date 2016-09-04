package com.jaodevelop.google_speech_api_android.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaodevelop.google_speech_api_android.R;
import com.jaodevelop.google_speech_api_android.google.GoogleSpeech;

import java.util.List;

/**
 * Created by jao on 9/3/16.
 */
public class ResultArrayAdapter extends ArrayAdapter<GoogleSpeech.SynRecognizeAlternative> {

    private Context mContext;
    private List<GoogleSpeech.SynRecognizeAlternative> mValues;

    public ResultArrayAdapter(Context context, List<GoogleSpeech.SynRecognizeAlternative> values) {

        super(context, -1, values);

        this.mContext = context;
        this.mValues = values;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listitem_alternative, parent, false);

        TextView tvTranscript = (TextView) rowView.findViewById(R.id.li_alternative_tv_transcript);
        TextView tvConfidence = (TextView) rowView.findViewById(R.id.li_alternative_tv_confidence);

        tvTranscript.setText(mValues.get(position).getTranscript());
        tvConfidence.setText(mValues.get(position).getConfidence().toString());

        return rowView;
    }

}

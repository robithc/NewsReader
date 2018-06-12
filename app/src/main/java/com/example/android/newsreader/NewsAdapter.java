package com.example.android.newsreader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class NewsAdapter extends ArrayAdapter<News> {

    public NewsAdapter(@NonNull Context context, ArrayList<News> newsItems) {
        super(context, 0, newsItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate
                    (R.layout.list_item, parent, false);
        }

        News currentNewsItem = getItem(position);
        String date = currentNewsItem.getDate();
        String author = currentNewsItem.getAuthor();
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        titleTextView.setText(currentNewsItem.getTitle());

        TextView sectionTextView = (TextView) listItemView.findViewById(R.id.section);
        sectionTextView.setText(currentNewsItem.getSection());

        TextView dateTextView = (TextView) listItemView.findViewById(R.id.date);
        setDateTextView(dateTextView, date);

        TextView authorTextView = (TextView) listItemView.findViewById(R.id.author);
        setAuthorTextView(authorTextView, author);

        return listItemView;
    }

    private void setAuthorTextView(TextView authorTextView, String author) {
        if (author == null) {
            authorTextView.setVisibility(View.GONE);
        } else {
            authorTextView.setVisibility(View.VISIBLE);
            authorTextView.setText(author);
        }
    }

    private void setDateTextView(TextView dateTextView, String date) {
        if (date == null) {
            dateTextView.setVisibility(View.GONE);
        } else {
            dateTextView.setVisibility(View.VISIBLE);
            SpannableString newDate = new SpannableString(date);
            newDate.setSpan(new UnderlineSpan(), 0, newDate.length(), 0);
            dateTextView.setText(newDate);
        }
    }
}
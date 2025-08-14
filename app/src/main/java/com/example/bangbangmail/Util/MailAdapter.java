package com.example.bangbangmail.Util;

import android.content.Context;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bangbangmail.MailDetail;
import com.example.bangbangmail.Model.Mail;
import com.example.bangbangmail.R;

import java.util.List;

/**
 * Created by Kenshin on 2017/5/21.
 */

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.ViewHolder>{
    private Context mContext;

    private List<Mail> mMailList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView formText;
        TextView timeText;
        TextView subjectText;
        TextView contentText;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            formText = (TextView) view.findViewById(R.id.mail_address);
            timeText = (TextView) view.findViewById(R.id.recv_time);
            subjectText = (TextView) view.findViewById(R.id.mail_subject);
            contentText = (TextView) view.findViewById(R.id.mail_content);
        }
    }

    public MailAdapter(List<Mail> mailList) {
        mMailList = mailList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.mail_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Mail mail = mMailList.get(position);
                Log.d("MailAdapter", "onClick: " + position);
                Intent intent = new Intent(mContext, MailDetail.class);
                Log.d("MailAdapter", "onClick: " + mContext);
                intent.putExtra("CONTEXT", mContext.toString());
                intent.putExtra("MAILID", mail.getMailID());
                intent.putExtra("POSITION", mMailList.size() - position - 1);
                intent.putExtra("FROM", mail.getFrom());
                intent.putExtra("TO", mail.getTo());
                intent.putExtra("SUBJECT", mail.getSubject());
                intent.putExtra("DATE", mail.getDate());
                intent.putExtra("TEXT", mail.getText());
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Mail mail = mMailList.get(position);
        holder.formText.setText(mail.getFrom());
        holder.timeText.setText(mail.getDate());
        holder.subjectText.setText(mail.getSubject());
        holder.contentText.setText(mail.getText());
    }

    @Override
    public int getItemCount() {
        return mMailList.size();
    }
}

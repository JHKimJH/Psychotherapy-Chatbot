package com.bae.dialogflowbot.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bae.dialogflowbot.R;
import com.bae.dialogflowbot.models.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

  private List<Message> messageList;
  private Activity activity;
  private final Context mContext;

  List<String> piclist = new ArrayList<>(Arrays.asList("sea", "river", "tree", "rain", "forest"));
  String pic;



  public ChatAdapter(List<Message> messageList, Activity activity ) {
    this.messageList = messageList;
    this.activity = activity;
    mContext = activity.getApplicationContext();
  }

  @NonNull @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(activity).inflate(R.layout.adapter_message_one, parent, false);
    return new MyViewHolder(view);
  }
  int count = 0;


  private class ImageGetter implements Html.ImageGetter {
    public Drawable getDrawable(String source) {
      source = source.replaceAll(".png", "");  //이때 .png .jpg 등이 붙어 있으면 drawable에서 이미지를 못불러 온다.

      int id = mContext.getResources().getIdentifier(source, "drawable", mContext.getPackageName());
      Drawable d = mContext.getResources().getDrawable(id);
      int w = d.getIntrinsicWidth();
      int h = d.getIntrinsicHeight();
      d.setBounds(30,5, w, h); //이미지 크기 설정
      return d;
    }
  };


  @Override public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    String message = messageList.get(position).getMessage();
    boolean isReceived = messageList.get(position).getIsReceived();
     if(isReceived){
       holder.messageReceive.setVisibility(View.VISIBLE);
       holder.messageSend.setVisibility(View.GONE);
       if(message.contains("<img src='")) {
         holder.messageReceive.setText(Html.fromHtml(message, new ImageGetter(), null));
       }
       else{
         holder.messageReceive.setText(message);
       }
     }else {
         holder.messageSend.setVisibility(View.VISIBLE);
         holder.messageReceive.setVisibility(View.GONE);
         holder.messageSend.setText(message);
     }
  }

  @Override public int getItemCount() {
    return messageList.size();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder{

    TextView messageSend;
    TextView messageReceive;

    MyViewHolder(@NonNull View itemView) {
      super(itemView);
      messageSend = itemView.findViewById(R.id.message_send);
      messageReceive = itemView.findViewById(R.id.message_receive);

    }
  }

}

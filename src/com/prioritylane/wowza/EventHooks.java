package com.prioritylane.wowza;


import java.io.IOException;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.cupertinostreaming.httpstreamer.HTTPStreamerSessionCupertino;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.httpstreamer.smoothstreaming.httpstreamer.HTTPStreamerSessionSmoothStreamer;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.rtp.model.RTPSession;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify3;
import com.wowza.wms.vhost.IVHost;

import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
 

public class EventHooks extends ModuleBase {

	private IApplicationInstance appInstance = null;
	private IVHost vhost = null;
	
	public void doSomething(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("##########");
		sendResult(client, params, "Hello Wowza");
	}

	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("+++++ onAppStart: " + fullname);
		this.appInstance = appInstance;
		this.vhost = appInstance.getVHost();

	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("++++++ onAppStop: " + fullname);
		try {
			Unirest.shutdown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	class StreamListener implements IMediaStreamActionNotify3
	{
		IVHost vhost = null;
		IApplicationInstance appInstance = null;
		
		public StreamListener ( IVHost ivhost, IApplicationInstance appins )
		{
			this.vhost = ivhost;
			this.appInstance = appins;
		}
		
		public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) { }
		public void onPauseRaw(IMediaStream stream, boolean isPause, double location) { }
		public void onPause(IMediaStream stream, boolean isPause, double location) { }
		public void onPlay(IMediaStream stream, String streamName, double playStart, double playLen, int playReset) { }
		public void onSeek(IMediaStream stream, double location) { }
		public void onStop(IMediaStream stream) { }
		public void onCodecInfoAudio(IMediaStream stream,MediaCodecInfoAudio codecInfoAudio) { }
		public void onCodecInfoVideo(IMediaStream stream,MediaCodecInfoVideo codecInfoVideo) { }

		public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
		{
			getLogger().info("###### onPublish: " +  streamName);
			
			try {
				@SuppressWarnings("unused")
				com.mashape.unirest.http.HttpResponse<JsonNode>  jsonResponse = Unirest.get("http://stage.performanceroom.com/mediaserver_publish")
						  .header("accept", "application/text")
						  .queryString("name", streamName)
						  .asJson();
			} catch (UnirestException e) {
				getLogger().error("++++++ EventHooks:onPublish failed: " + e.toString());
				e.printStackTrace();
			}
			
//			getLogger().info("###### onPublish: " + streamName);
			
			
		}

		public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
		{
			getLogger().info("###### onUnPublish: " + streamName);
 		}

	}

	@SuppressWarnings({ "unchecked", "unused" })
	public void onStreamCreate(IMediaStream stream)
	{

		IMediaStreamActionNotify3 actionNotify = new StreamListener(this.vhost,this.appInstance);
		WMSProperties props = stream.getProperties();
		synchronized (props)
		{
			props.put("streamActionNotifier", actionNotify);
		}
		stream.addClientListener(actionNotify);
		
		try {
			com.mashape.unirest.http.HttpResponse<JsonNode> jsonResponse = Unirest.post("http://httpbin.org/post")
					  .header("accept", "application/json")
					  .queryString("apiKey", "123")
					  .field("parameter", "value")
					  .field("foo", "bar")
					  .asJson();
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onStreamDestroy(IMediaStream stream)
	{
		IMediaStreamActionNotify3 actionNotify = null;
		WMSProperties props = stream.getProperties();
		synchronized (props)
		{
			actionNotify = (IMediaStreamActionNotify3) stream.getProperties().get("streamActionNotifier");
		}
		if (actionNotify != null)
		{
			stream.removeClientListener(actionNotify);
		}
	}
	
	
	public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("###### onConnect: " + client.getClientId());
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("###### onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("###### onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("###### onDisconnect: " + client.getClientId());
	}

//	public void onStreamCreate(IMediaStream stream) {
//		getLogger().info("onStreamCreate: " + stream.getSrc());
//	}
//
//	public void onStreamDestroy(IMediaStream stream) {
//		getLogger().info("onStreamDestroy: " + stream.getSrc());
//	}

	public void onHTTPSessionCreate(IHTTPStreamerSession httpSession) {
		getLogger().info("##### onHTTPSessionCreate: " + httpSession.getSessionId());
	}

	public void onHTTPSessionDestroy(IHTTPStreamerSession httpSession) {
		getLogger().info("##### onHTTPSessionDestroy: " + httpSession.getSessionId());
	}

	public void onHTTPCupertinoStreamingSessionCreate(HTTPStreamerSessionCupertino httpSession) {
		getLogger().info("##### onHTTPCupertinoStreamingSessionCreate: " + httpSession.getSessionId());
	}

	public void onHTTPCupertinoStreamingSessionDestroy(HTTPStreamerSessionCupertino httpSession) {
		getLogger().info("##### onHTTPCupertinoStreamingSessionDestroy: " + httpSession.getSessionId());
	}

	public void onHTTPSmoothStreamingSessionCreate(HTTPStreamerSessionSmoothStreamer httpSession) {
		getLogger().info("##### onHTTPSmoothStreamingSessionCreate: " + httpSession.getSessionId());
	}

	public void onHTTPSmoothStreamingSessionDestroy(HTTPStreamerSessionSmoothStreamer httpSession) {
		getLogger().info("##### onHTTPSmoothStreamingSessionDestroy: " + httpSession.getSessionId());
	}

	public void onRTPSessionCreate(RTPSession rtpSession) {
		getLogger().info("##### onRTPSessionCreate: " + rtpSession.getSessionId());
	}

	public void onRTPSessionDestroy(RTPSession rtpSession) {
		getLogger().info("##### onRTPSessionDestroy: " + rtpSession.getSessionId());
	}

	public void onCall(String handlerName, IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("##### onCall: " + handlerName);
	}

}

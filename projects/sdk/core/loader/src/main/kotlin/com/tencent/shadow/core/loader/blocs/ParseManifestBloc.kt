package com.tencent.shadow.core.loader.blocs

import android.content.IntentFilter
import android.content.res.Resources
import com.tencent.shadow.core.loader.infos.ManifestInfo
import com.tencent.shadow.core.loader.infos.ManifestInfo.Receiver
import com.tencent.shadow.core.loader.infos.ManifestInfo.ReceiverIntentInfo
import org.xmlpull.v1.XmlPullParser

/**
 * 解析插件的AndroidManifest.xml文件
 * 由于系统开放接口不提供广播的action信息，此处采用手动解析的方式处理,减少插件化的适配工作
 * 后续对于AndroidManifest.xml的处理可在此基础上扩展
 *
 * @author xuedizi2009@163.com
 */
object ParseManifestBloc {

  private const val ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android"
  private const val TAG_RECEIVER = "receiver"
  private const val TAG_INTENT_FILTER = "intent-filter"
  private const val TAG_ACTION = "action"
  private const val ATTR_NAME = "name"

  @Throws(Exception::class)
  fun parse(resources: Resources): ManifestInfo = ManifestInfo().apply {
    val parser = resources.assets.openXmlResourceParser("AndroidManifest.xml")
    val outerDepth = parser.depth
    var type: Int
    while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT
      && (type != XmlPullParser.END_TAG || parser.depth > outerDepth)
    ) {
      if (type == XmlPullParser.START_TAG) {
        parseBroadcastReceiver(parser, this)
      }
    }
  }

  private fun parseBroadcastReceiver(parser: XmlPullParser, manifestInfo: ManifestInfo) {
    if (TAG_RECEIVER == parser.name) {
      val receiver = Receiver(parser.getAttributeValue(ANDROID_RESOURCES, ATTR_NAME))
      val outerDepth = parser.depth
      var type: Int
      while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT
        && (type != XmlPullParser.END_TAG
          || parser.depth > outerDepth)) {
        if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
          continue
        }
        if (TAG_INTENT_FILTER == parser.name) {
          val receiverInfo = ReceiverIntentInfo()
          parserIntent(parser, receiverInfo)
          receiver.intents.add(receiverInfo)
        }
      }
      manifestInfo.receivers.add(receiver)
    }
  }

  private fun parserIntent(parser: XmlPullParser, intentFilter: IntentFilter) {
    val outerDepth = parser.depth
    var type: Int
    while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT
      && (type != XmlPullParser.END_TAG || parser.depth > outerDepth)
    ) {
      if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
        continue
      }
      if (TAG_ACTION == parser.name) {
        val value = parser.getAttributeValue(ANDROID_RESOURCES, ATTR_NAME)
        intentFilter.addAction(value)
      }
    }
  }
}
package com.castlabs.dash.helpers;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.boxes.dolby.*;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.AudioSpecificConfig;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.DecoderConfigDescriptor;
import org.mp4parser.boxes.iso14496.part12.OriginalFormatBox;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import org.mp4parser.boxes.iso14496.part30.XMLSubtitleSampleEntry;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.Edit;
import org.mp4parser.muxer.Track;
import org.mp4parser.tools.Hex;
import org.mp4parser.tools.Path;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gets the precise MIME type according to RFC6381.
 * http://tools.ietf.org/html/rfc6381
 */
public final class DashHelper2 {

    public static long getAudioSamplingRate(AudioSampleEntry e) {
        ESDescriptorBox esds = Path.getPath(e, "esds");
        if (esds != null) {
            final DecoderConfigDescriptor decoderConfigDescriptor = esds.getEsDescriptor().getDecoderConfigDescriptor();
            final AudioSpecificConfig audioSpecificConfig = decoderConfigDescriptor.getAudioSpecificInfo();
            if (audioSpecificConfig.getExtensionAudioObjectType() > 0 && audioSpecificConfig.sbrPresentFlag) {
                return audioSpecificConfig.getExtensionSamplingFrequency();
            } else {
                return audioSpecificConfig.getSamplingFrequency();
            }
        } else {
            return e.getSampleRate();
        }
    }

    public static ChannelConfiguration getChannelConfiguration(AudioSampleEntry e) {
        DTSSpecificBox ddts = Path.getPath(e, "ddts");
        if (ddts != null) {
            return getDTSChannelConfig(e, ddts);
        }
        MLPSpecificBox dmlp = Path.getPath(e, "dmlp");
        if (dmlp != null) {
            return null; // getMLPChannelConfig(e, dmlp);
        }
        ESDescriptorBox esds = Path.getPath(e, "esds");
        if (esds != null) {
            return getAACChannelConfig(e, esds);
        }
        esds = Path.getPath(e, "..../esds"); // Apple does weird things
        if (esds != null) {
            return getAACChannelConfig(e, esds);
        }
        AC3SpecificBox dac3 = Path.getPath(e, "dac3");
        if (dac3 != null) {
            return getAC3ChannelConfig(e, dac3);
        }
        EC3SpecificBox dec3 = Path.getPath(e, "dec3");
        if (dec3 != null) {
            return getEC3ChannelConfig(e, dec3);
        }

        return null;
    }

    private static ChannelConfiguration getEC3ChannelConfig(AudioSampleEntry e, EC3SpecificBox dec3) {
        final List<EC3SpecificBox.Entry> ec3SpecificBoxEntries = dec3.getEntries();
        int audioChannelValue = 0;
        for (EC3SpecificBox.Entry ec3SpecificBoxEntry : ec3SpecificBoxEntries) {
            audioChannelValue |= getDolbyAudioChannelValue(ec3SpecificBoxEntry.acmod, ec3SpecificBoxEntry.lfeon, ec3SpecificBoxEntry.chan_loc);
        }
        ChannelConfiguration cc = new ChannelConfiguration();
        cc.value = Hex.encodeHex(new byte[]{(byte) ((audioChannelValue >> 8) & 0xFF), (byte) (audioChannelValue & 0xFF)});
        cc.schemeIdUri = "urn:dolby:dash:audio_channel_configuration:2011";
        return cc;
    }

    private static ChannelConfiguration getAC3ChannelConfig(AudioSampleEntry e, AC3SpecificBox dac3) {
        ChannelConfiguration cc = new ChannelConfiguration();
        int audioChannelValue = getDolbyAudioChannelValue(dac3.getAcmod(), dac3.getLfeon(), 0);
        cc.value = Hex.encodeHex(new byte[]{(byte) ((audioChannelValue >> 8) & 0xFF), (byte) (audioChannelValue & 0xFF)});
        cc.schemeIdUri = "urn:dolby:dash:audio_channel_configuration:2011";
        return cc;
    }

    private static int getDolbyAudioChannelValue(int acmod, int lfeon, int chan_loc) {
        int audioChannelValue;
        switch (acmod) {
            case 0:
                audioChannelValue = 0xA000;
                break;
            case 1:
                audioChannelValue = 0x4000;
                break;
            case 2:
                audioChannelValue = 0xA000;
                break;
            case 3:
                audioChannelValue = 0xE000;
                break;
            case 4:
                audioChannelValue = 0xA100;
                break;
            case 5:
                audioChannelValue = 0xE100;
                break;
            case 6:
                audioChannelValue = 0xB800;
                break;
            case 7:
                audioChannelValue = 0xF800;
                break;
            default:
                throw new RuntimeException("Unexpected acmod " + acmod);
        }
        if (lfeon == 1) {
            audioChannelValue += 1;
        }
        int[] chanLoc2audioChannelConfiguration = new int[]{
                0b0000010000000000, // 0 - Lc/Rc
                0b0000001000000000, // 1 - Lls/Lrs
                0b0000000100000000, // 2 - Cs
                0b0000000010000000, // 3 - Ts
                0b0000000001000000, // 4 - Lsd/Rsd
                0b0000000000100000, // 5 - Lw/Rw
                0b0000000000010000, // 6 - Lvh/Rvh
                0b0000000000001000, // 7 - Cvh
                0b0000000000000010, // 8 - LFE2
        };
        for (int i = 0; i < chanLoc2audioChannelConfiguration.length; i++) {
            if ((chan_loc & (0b000000001 << i)) > 0) {
                audioChannelValue |= chanLoc2audioChannelConfiguration[i];
            }
        }

        return audioChannelValue;
    }


    public static double getEarliestTrackPresentationTime(List<Edit> edits) {
        double earliestTrackPresentationTime = 0;
        boolean acceptEdit = true;
        boolean acceptDwell = true;
        for (Edit edit : edits) {
            if (edit.getMediaTime() == -1 && !acceptDwell) {
                throw new RuntimeException("Cannot accept edit list for processing (1)");
            }
            if (edit.getMediaTime() >= 0 && !acceptEdit) {
                throw new RuntimeException("Cannot accept edit list for processing (2)");
            }
            if (edit.getMediaTime() == -1) {
                earliestTrackPresentationTime += edit.getSegmentDuration();
            } else /* if edit.getMediaTime() >= 0 */ {
                earliestTrackPresentationTime -= (double) edit.getMediaTime() / edit.getTimeScale();
                acceptEdit = false;
                acceptDwell = false;
            }
        }
        return earliestTrackPresentationTime;
    }

    private static ChannelConfiguration getAACChannelConfig(AudioSampleEntry e, ESDescriptorBox esds) {

        final DecoderConfigDescriptor decoderConfigDescriptor = esds.getEsDescriptor().getDecoderConfigDescriptor();
        final AudioSpecificConfig audioSpecificConfig = decoderConfigDescriptor.getAudioSpecificInfo();
        ChannelConfiguration cc = new ChannelConfiguration();
        cc.schemeIdUri = "urn:mpeg:dash:23003:3:audio_channel_configuration:2011";
        cc.value = "2";
        if (audioSpecificConfig != null && audioSpecificConfig.getChannelConfiguration() > 2) {
            // in case of mono let's assume stereo as it will be Parametric Stereo in most cases.
            cc.value = String.valueOf(audioSpecificConfig.getChannelConfiguration());
        }
        return cc;
    }

    /**
     * Returns the number of frame which correspond to the time given.
     */
    public static int time2Frames(Track track, double timeInSeconds) {
        int i = 0;
        while (timeInSeconds > 0) {
            timeInSeconds -= (double) track.getSampleDurations()[i] / track.getTrackMetaData().getTimescale();
            i++;
        }
        return i;
    }

    private static int getNumChannels(DTSSpecificBox dtsSpecificBox) {
        final int channelLayout = dtsSpecificBox.getChannelLayout();
        int numChannels = 0;
        int dwChannelMask = 0;
        if ((channelLayout & 0x0001) == 0x0001) {
            //0001h Center in front of listener 1
            numChannels += 1;
            dwChannelMask |= 0x00000004; //SPEAKER_FRONT_CENTER
        }
        if ((channelLayout & 0x0002) == 0x0002) {
            //0002h Left/Right in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000001; //SPEAKER_FRONT_LEFT
            dwChannelMask |= 0x00000002; //SPEAKER_FRONT_RIGHT
        }
        if ((channelLayout & 0x0004) == 0x0004) {
            //0004h Left/Right surround on side in rear 2
            numChannels += 2;
            //* if Lss, Rss exist, then this position is equivalent to Lsr, Rsr respectively
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x0008) == 0x0008) {
            //0008h Low frequency effects subwoofer 1
            numChannels += 1;
            dwChannelMask |= 0x00000008; //SPEAKER_LOW_FREQUENCY
        }
        if ((channelLayout & 0x0010) == 0x0010) {
            //0010h Center surround in rear 1
            numChannels += 1;
            dwChannelMask |= 0x00000100; //SPEAKER_BACK_CENTER
        }
        if ((channelLayout & 0x0020) == 0x0020) {
            //0020h Left/Right height in front 2
            numChannels += 2;
            dwChannelMask |= 0x00001000; //SPEAKER_TOP_FRONT_LEFT
            dwChannelMask |= 0x00004000; //SPEAKER_TOP_FRONT_RIGHT
        }
        if ((channelLayout & 0x0040) == 0x0040) {
            //0040h Left/Right surround in rear 2
            numChannels += 2;
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x0080) == 0x0080) {
            //0080h Center Height in front 1
            numChannels += 1;
            dwChannelMask |= 0x00002000; //SPEAKER_TOP_FRONT_CENTER
        }
        if ((channelLayout & 0x0100) == 0x0100) {
            //0100h Over the listener’s head 1
            numChannels += 1;
            dwChannelMask |= 0x00000800; //SPEAKER_TOP_CENTER
        }
        if ((channelLayout & 0x0200) == 0x0200) {
            //0200h Between left/right and center in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000040; //SPEAKER_FRONT_LEFT_OF_CENTER
            dwChannelMask |= 0x00000080; //SPEAKER_FRONT_RIGHT_OF_CENTER
        }
        if ((channelLayout & 0x0400) == 0x0400) {
            //0400h Left/Right on side in front 2
            numChannels += 2;
            dwChannelMask |= 0x00000200; //SPEAKER_SIDE_LEFT
            dwChannelMask |= 0x00000400; //SPEAKER_SIDE_RIGHT
        }
        if ((channelLayout & 0x0800) == 0x0800) {
            //0800h Left/Right surround on side 2
            numChannels += 2;
            //* if Lss, Rss exist, then this position is equivalent to Lsr, Rsr respectively
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x1000) == 0x1000) {
            //1000h Second low frequency effects subwoofer 1
            numChannels += 1;
            dwChannelMask |= 0x00000008; //SPEAKER_LOW_FREQUENCY
        }
        if ((channelLayout & 0x2000) == 0x2000) {
            //2000h Left/Right height on side 2
            numChannels += 2;
            dwChannelMask |= 0x00000010; //SPEAKER_BACK_LEFT
            dwChannelMask |= 0x00000020; //SPEAKER_BACK_RIGHT
        }
        if ((channelLayout & 0x4000) == 0x4000) {
            //4000h Center height in rear 1
            numChannels += 1;
            dwChannelMask |= 0x00010000; //SPEAKER_TOP_BACK_CENTER
        }
        if ((channelLayout & 0x8000) == 0x8000) {
            //8000h Left/Right height in rear 2
            numChannels += 2;
            dwChannelMask |= 0x00008000; //SPEAKER_TOP_BACK_LEFT
            dwChannelMask |= 0x00020000; //SPEAKER_TOP_BACK_RIGHT
        }
        if ((channelLayout & 0x10000) == 0x10000) {
            //10000h Center below in front
            numChannels += 1;
        }
        if ((channelLayout & 0x20000) == 0x20000) {
            //20000h Left/Right below in front
            numChannels += 2;
        }
        return numChannels;
    }

    private static ChannelConfiguration getDTSChannelConfig(AudioSampleEntry e, DTSSpecificBox ddts) {
        ChannelConfiguration cc = new ChannelConfiguration();
        cc.value = Integer.toString(getNumChannels(ddts));
        cc.schemeIdUri = "urn:dts:dash:audio_channel_configuration:2012";
        return cc;
    }

    /**
     * Gets the codec according to RFC 6381 from a <code>SampleEntry</code>.
     *
     * @param se <code>SampleEntry</code> to id the codec.
     * @return codec according to RFC
     */
    public static String getRfc6381Codec(SampleEntry se) {

        OriginalFormatBox frma = Path.getPath((Box) se, "sinf/frma");
        String type;
        if (frma != null) {
            type = frma.getDataFormat();
        } else {
            type = se.getType();
        }


        if ("avc1".equals(type) || "avc2".equals(type) || "avc3".equals(type) || "avc4".equals(type)) {
            AvcConfigurationBox avcConfigurationBox = Path.getPath((Box) se, "avcC");
            List<ByteBuffer> spsbytes = avcConfigurationBox.getSequenceParameterSets();
            byte[] CodecInit = new byte[3];
            CodecInit[0] = spsbytes.get(0).get(1);
            CodecInit[1] = spsbytes.get(0).get(2);
            CodecInit[2] = spsbytes.get(0).get(3);
            return (type + "." + Hex.encodeHex(CodecInit)).toLowerCase();
        } else if (type.equals("mp4a")) {
            ESDescriptorBox esDescriptorBox = Path.getPath((Box) se, "esds");
            if (esDescriptorBox == null) {
                esDescriptorBox = Path.getPath((Box) se, "..../esds"); // Apple does weird things
            }
            final DecoderConfigDescriptor decoderConfigDescriptor = esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor();
            final AudioSpecificConfig audioSpecificConfig = decoderConfigDescriptor.getAudioSpecificInfo();
            if (audioSpecificConfig != null && audioSpecificConfig.sbrPresentFlag && !audioSpecificConfig.psPresentFlag) {
                return "mp4a.40.5";
            } else if (audioSpecificConfig != null && audioSpecificConfig.sbrPresentFlag && audioSpecificConfig.psPresentFlag) {
                return "mp4a.40.29";
            } else {
                return "mp4a.40.2";
            }
        } else if (type.equals("mp4v")) {
            ESDescriptorBox esDescriptorBox = Path.getPath((Box) se, "esds");
            if (esDescriptorBox == null) {
                esDescriptorBox = Path.getPath((Box) se, "..../esds"); // Apple does weird things
            }
            if (esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor().getObjectTypeIndication() == 0x6C) {
                return "mp4v." +
                        Integer.toHexString(esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor().getObjectTypeIndication());
            } else {
                throw new RuntimeException("I don't know how to construct codec for mp4v with OTI " +
                        esDescriptorBox.getEsDescriptor().getDecoderConfigDescriptor().getObjectTypeIndication()
                );
            }
        } else if (type.equals("dtsl") || type.equals("dtsl") || type.equals("dtse")) {
            return type;
        } else if (type.equals("ec-3") || type.equals("ac-3") || type.equals("mlpa")) {
            return type;
        } else if (type.equals("hev1") || type.equals("hvc1")) {
            int c;
            HevcConfigurationBox hvcc = Path.getPath((Box) se, "hvcC");

            String codec = type + ".";

            if (hvcc.getGeneral_profile_space() == 1) {
                codec += "A";
            } else if (hvcc.getGeneral_profile_space() == 2) {
                codec += "B";
            } else if (hvcc.getGeneral_profile_space() == 3) {
                codec += "C";
            }
            //profile idc encoded as a decimal number
            codec += hvcc.getGeneral_profile_idc();
            //general profile compatibility flags: hexa, bit-reversed
            {
                long val = hvcc.getGeneral_profile_compatibility_flags();
                long i, res = 0;
                for (i = 0; i < 31; i++) {
                    res |= val & 1;
                    res <<= 1;
                    val >>= 1;
                }
                res |= val & 1;
                codec += ".";
                codec += Long.toHexString(res);
            }

            if (hvcc.isGeneral_tier_flag()) {
                codec += ".H";
            } else {
                codec += ".L";
            }
            codec += hvcc.getGeneral_level_idc();


            long _general_constraint_indicator_flags = hvcc.getGeneral_constraint_indicator_flags();
            if (hvcc.getHevcDecoderConfigurationRecord().isFrame_only_constraint_flag()) {
                _general_constraint_indicator_flags |= 1l << 47;
            }
            if (hvcc.getHevcDecoderConfigurationRecord().isNon_packed_constraint_flag()) {
                _general_constraint_indicator_flags |= 1l << 46;
            }
            if (hvcc.getHevcDecoderConfigurationRecord().isInterlaced_source_flag()) {
                _general_constraint_indicator_flags |= 1l << 45;
            }
            if (hvcc.getHevcDecoderConfigurationRecord().isProgressive_source_flag()) {
                _general_constraint_indicator_flags |= 1l << 44;
            }

            codec += "." + hexByte(_general_constraint_indicator_flags >> 40);


            if ((_general_constraint_indicator_flags & 0xFFFFFFFFFFL) > 0) {
                codec += "." + hexByte(_general_constraint_indicator_flags >> 32);

                if ((_general_constraint_indicator_flags & 0xFFFFFFFFL) > 0) {
                    codec += "." + hexByte(_general_constraint_indicator_flags >> 24);
                    if ((_general_constraint_indicator_flags & 0xFFFFFFL) > 0) {
                        codec += "." + hexByte(_general_constraint_indicator_flags >> 16);
                        if (((_general_constraint_indicator_flags & 0xFFFFL)) > 0) {
                            codec += "." + hexByte(_general_constraint_indicator_flags >> 8);
                            if (((_general_constraint_indicator_flags & 0xFFL)) > 0) {
                                codec += "." + hexByte(_general_constraint_indicator_flags);
                            }
                        }
                    }
                }
            }

            return codec;
        } else if (type.equals("stpp")) {
            XMLSubtitleSampleEntry stpp = (XMLSubtitleSampleEntry) se;
            if (stpp.getSchemaLocation().contains("cff-tt-text-ttaf1-dfxp")) {
                return "cfft";
            } else if (stpp.getSchemaLocation().contains("cff-tt-image-ttaf1-dfxp")) {
                return "cffi";
            } else {
                return "stpp";
            }

        } else if (type.equals("dvav") || type.equals("dva1") || type.equals("dvhe") || type.equals("dvh1")) {
            DoViConfigurationBox dvcC = Path.getPath((Box) se, "dvcC");
            return type + String.format(".%02d.%02d", dvcC.getDvProfile(), dvcC.getDvLevel());
        } else {
            return null;
        }

    }

    static String hexByte(long l) {
        return Hex.encodeHex(new byte[]{(byte) (l & 0xFF)});
    }

    public static String getFormat(Track track) {
        List<SampleEntry> ses = track.getSampleEntries();
        String format = null;
        for (SampleEntry se : ses) {

            OriginalFormatBox frma = Path.getPath((Container) se, "sinf/frma");
            if (frma != null) {
                if (format == null || format.equals(frma.getDataFormat())) {
                    format = frma.getDataFormat();
                } else {
                    throw new RuntimeException("cant determine format of track");
                }

            } else {
                if (format == null || format.equals(se.getType())) {
                    format = se.getType();
                } else {
                    throw new RuntimeException("cant determine format of track");
                }
            }
        }
        return format;
    }

    public static class ChannelConfiguration {
        public String schemeIdUri = "";
        public String value = "";
    }

    public static Locale getTextTrackLocale(File textTrack) throws IOException {
        Pattern patternFilenameIncludesLanguage = Pattern.compile(".*[-_](.+)$");
        String ext = FilenameUtils.getExtension(textTrack.getName());
        String basename = FilenameUtils.getBaseName(textTrack.getName());
        if (ext.equals("vtt")) {
            Matcher m = patternFilenameIncludesLanguage.matcher(basename);
            if (m.matches()) {
                return Locale.forLanguageTag(m.group(1));
            } else {
                throw new IOException("Cannot determine language of " + textTrack + " please use the pattern filename-[language-tag].vtt");
            }
        } else if (ext.equals("xml") || ext.equals("dfxp") || ext.equals("ttml")) {
            DocumentBuilderFactory builderFactory =
                    DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = builderFactory.newDocumentBuilder();

                String xml = FileUtils.readFileToString(textTrack);
                Document xmlDocument = builder.parse(new ByteArrayInputStream(xml.getBytes()));

                String lang = xmlDocument.getDocumentElement().getAttribute("xml:lang");
                NodeList nl = xmlDocument.getDocumentElement().getElementsByTagName("div");
                for (int i = 0; i < nl.getLength(); i++) {
                    Attr langInDiv = (Attr) nl.item(i).getAttributes().getNamedItem("xml:lang");
                    if (langInDiv != null) {
                        lang = langInDiv.getValue();
                    }

                }
                if (lang != null) {
                    return Locale.forLanguageTag(lang);
                } else {
                    Matcher m2 = patternFilenameIncludesLanguage.matcher(basename);
                    if (m2.matches()) {
                        return Locale.forLanguageTag(m2.group(1));
                    } else {
                        throw new IOException("Cannot determine language of " + textTrack + " please use either the xml:lang attribute or a filename pattern like filename-[language-tag].[xml|dfxp]");
                    }
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                throw new IOException("Cannot instantiate XML parser to determine textTrack language");
            } catch (SAXException e) {
                e.printStackTrace();
                throw new IOException("Cannot parse XML to extract text track's language");
            }


        } else {
            throw new IOException("Unknown subtitle format in " + textTrack);
        }


    }

    public static String filename2UrlPath(String filename) {
        URI uri;
        try {
            uri = new URI(null,
                    null, null, -1,
                    filename, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri.toASCIIString();
    }
}

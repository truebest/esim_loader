package com.company;

import com.company.card.CardTerminalHandler;
import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.ApduTransmittedListener;
import com.truphone.lpa.impl.LocalProfileAssistantImpl;
import com.truphone.rsp.dto.asn1.rspdefinitions.EuiccConfiguredAddressesResponse;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.smartcardio.CardException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final String MY_SIM_READER = "ACS CCID USB Reader 0";
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final String CLA = "81";
    private static final String INSTRUCTION = "E2";
    private static final String P1_11 = "11";
    private static final String P1_91 = "91";
    private static final String P2 = "00";
    private static final int len = 120;
    private static LpaSrc lpa;
    private static List<String> terminalNames;

    private static void refreshReadersList() throws CardException {
        terminalNames = CardTerminalHandler.getCardTerminalNames(true);
        for (String terminalName : terminalNames) {
            System.out.printf(terminalName + "\n");
        }
    }

    private static boolean connectActionPerformed(String deviceName) {
        if (terminalNames.size() > 0) {
            for (String terminalName : terminalNames) {
                {
                    //String terminalName = terminalNames.get(i);
                    if (!terminalName.isEmpty() && terminalName.equals(deviceName)) ;
                    {
                        if (lpa != null) {
                            lpa.disconnect();
                        }
                        try {
                            lpa = new LpaSrc(terminalName);
                        } catch (CardException ex) {
                            LOG.log(Level.SEVERE, ex.toString());
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static EuiccConfiguredAddressesResponse getCurrentConfiguredAddressResponse() throws IOException, DecoderException {
        InputStream is;
        EuiccConfiguredAddressesResponse configuredAddress = new EuiccConfiguredAddressesResponse();
        is = new ByteArrayInputStream(Hex.decodeHex(lpa.getSMDPAddress().toCharArray()));
        configuredAddress.decode(is);

        return configuredAddress;
    }

    private static void updateEuiccInfo() throws DecoderException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Eid: ").append(lpa.getEID()).append(System.getProperty("line.separator"));

        InputStream is;
        EuiccConfiguredAddressesResponse configuredAddress = getCurrentConfiguredAddressResponse();
        String rootDsAddress = "", defaultSmdpAddress = "";

        //try {
        rootDsAddress = configuredAddress.getRootDsAddress() != null ? configuredAddress.getRootDsAddress().toString() : "";
        defaultSmdpAddress = configuredAddress.getDefaultDpAddress() != null ? configuredAddress.getDefaultDpAddress().toString() : "";
        //} catch (Exception ex) {
        //    LOG.log(Level.SEVERE, ex.toString());
        //    Util.showMessageDialog(this, String.format("Failed to get SMDP+ & SMDS addresses\nReason:%s\nCheck the logs for more info", ex.getMessage()));
        //}

        sb.append("Root SM-DS: ").append(rootDsAddress).append(System.getProperty("line.separator"));
        sb.append("Default SM-DP+: ").append(defaultSmdpAddress).append(System.getProperty("line.separator"));
        System.out.println(sb);
    }

    private static void readInfo()
    {
        try {
            updateEuiccInfo();
        } catch (IOException | DecoderException ex) {
            LOG.log(Level.WARNING, ex.toString());
            System.out.println(String.format("Failed to read card info \nReason: %s \nPlease check the log for more info.", ex.getMessage()));
        }
    }

    public static void main(String[] args) {
        try {
            refreshReadersList();
            if (connectActionPerformed(MY_SIM_READER))
            {
                readInfo();
            }

        } catch (CardException e) {
            LOG.log(Level.SEVERE, e.toString());
        }
    }
}

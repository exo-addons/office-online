package org.exoplatform.officeonline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WOPIDiscovery {

  @JacksonXmlProperty(localName = "net-zone")
  private NetZone  netZone;

  @JacksonXmlProperty(localName = "proof-key")
  private ProofKey proofKey;

  public NetZone getNetZone() {
    return netZone;
  }

  public void setNetZone(NetZone netZone) {
    this.netZone = netZone;
  }

  public ProofKey getProofKey() {
    return proofKey;
  }

  public void setProofKey(ProofKey proofKey) {
    this.proofKey = proofKey;
  }

  protected static final XmlMapper XML_MAPPER = new XmlMapper();

  public static WOPIDiscovery read(byte[] discoveryBytes) throws IOException {
    return XML_MAPPER.readValue(new ByteArrayInputStream(discoveryBytes), WOPIDiscovery.class);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class NetZone {

    @JacksonXmlProperty(localName = "app")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<App> apps;

    public List<App> getApps() {
      return apps;
    }

    public void setApps(List<App> apps) {
      this.apps = apps;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ProofKey {

    private String exponent;

    private String modulus;

    @JacksonXmlProperty(localName = "oldexponent")
    private String oldExponent;

    @JacksonXmlProperty(localName = "oldmodulus")
    private String oldModulus;

    public String getExponent() {
      return exponent;
    }

    public void setExponent(String exponent) {
      this.exponent = exponent;
    }

    public String getModulus() {
      return modulus;
    }

    public void setModulus(String modulus) {
      this.modulus = modulus;
    }

    public String getOldExponent() {
      return oldExponent;
    }

    public void setOldExponent(String oldExponent) {
      this.oldExponent = oldExponent;
    }

    public String getOldModulus() {
      return oldModulus;
    }

    public void setOldModulus(String oldModulus) {
      this.oldModulus = oldModulus;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class App {

    private String       name;

    @JacksonXmlProperty(localName = "action")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Action> actions;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<Action> getActions() {
      return actions;
    }

    public void setActions(List<Action> actions) {
      this.actions = actions;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Action {
    private String name;

    private String ext;

    @JacksonXmlProperty(localName = "urlsrc")
    private String url;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getExt() {
      return ext;
    }

    public void setExt(String ext) {
      this.ext = ext;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

  }
}

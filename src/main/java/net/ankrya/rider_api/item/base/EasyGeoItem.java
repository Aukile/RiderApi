package net.ankrya.rider_api.item.base;

public class EasyGeoItem extends BaseGeoItem {
    String model;
    String texture;
    public EasyGeoItem(Properties properties, String model, String texture) {
        super(properties);
        this.model = model;
        this.texture = texture;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getTexture() {
        return texture;
    }
}

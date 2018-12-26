package kz.incubator.sdcl.club1.about_us;

public class moderator {
    public int image;
    public String name;
    public String desc;
    public int color;

    public moderator(int image, String name, String desc, int color) {
        this.image = image;
        this.name = name;
        this.desc = desc;
        this.color = color;
    }

    public int getImage() {

        return image;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getColor() {
        return color;
    }
}

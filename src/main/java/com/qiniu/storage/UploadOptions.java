package com.qiniu.storage;

import com.qiniu.util.StringMap;

public final class UploadOptions {

    /**
     * 用于服务器上传回调通知的自定义参数，参数的key必须以x: 开头  eg: x:foo
     */
    public final StringMap params;

    /**
     * 用于设置meta数据，参数的key必须以x-qn-meta- 开头  eg: x-qn-meta-key
     */
    public final StringMap metaDataParam;

    /**
     * 指定上传文件的MimeType
     */
    public final String mimeType;

    /**
     * 启用上传内容crc32校验
     */
    public final boolean checkCrc;

    public static UploadOptions defaultOptions() {
        return new UploadOptions.Builder().build();
    }

    private UploadOptions(StringMap params,
                          StringMap metaDataParam,
                          String mimeType,
                          boolean checkCrc) {
        this.params = params;
        this.metaDataParam = metaDataParam;
        this.mimeType = mimeType;
        this.checkCrc = checkCrc;
    }


    public static class Builder {

        private StringMap params;
        private StringMap metaDataParam;
        private String mimeType;
        private boolean checkCrc;

        /**
         * 用于服务器上传回调通知的自定义参数，参数的key必须以x: 开头  eg: x:foo
         */
        public Builder params(StringMap params) {
            this.params = params;
            return this;
        }

        /**
         * 用于设置meta数据，参数的key必须以x-qn-meta- 开头  eg: x-qn-meta-key
         */
        public Builder metaData(StringMap params) {
            this.metaDataParam = params;
            return this;
        }

        /**
         * 指定上传文件的MimeType
         */
        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        /**
         * 启用上传内容crc32校验
         */
        public Builder checkCrc(boolean checkCrc) {
            this.checkCrc = checkCrc;
            return this;
        }

        public UploadOptions build() {
            mimeType = mime(mimeType);
            params = filterParam(params);
            metaDataParam = filterMetaData(metaDataParam);
            return new UploadOptions(params, metaDataParam, mimeType, checkCrc);
        }

        private String mime(String mimeType) {
            if (mimeType == null || mimeType.equals("")) {
                return "application/octet-stream";
            }
            return mimeType;
        }

        /**
         * 过滤用户自定义参数，只有参数名以<code>x:</code>开头的参数才会被使用
         *
         * @param params 待过滤的用户自定义参数
         * @return 过滤后的用户自定义参数
         */
        private StringMap filterParam(StringMap params) {
            final StringMap ret = new StringMap();
            if (params == null) {
                return ret;
            }

            params.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    ret.putWhen(key, value, key != null && key.startsWith("x:") && value != null && !value.equals(""));
                }
            });
            return ret;
        }

        /**
         * 过滤meta data参数，只有参数名以<code>x-qn-meta-</code>开头的参数才会被使用
         *
         * @param params 待过滤的用户自定义参数
         * @return 过滤后的参数
         */
        private StringMap filterMetaData(StringMap params) {
            final StringMap ret = new StringMap();
            if (params == null) {
                return ret;
            }

            params.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    ret.putWhen(key, value, key != null && key.startsWith("x-qn-meta-") && value != null && !value.equals(""));
                }
            });
            return ret;
        }
    }
}

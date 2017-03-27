一、 功能介绍
1. 支持文件上传下载，带参上传；
2. 支持文件断点下载；
3. 后续新功能之后提交。

二、代码示例（具体用法详见Demo）
1. 文件带参上传用法

        File file = new File(Environment.getExternalStorageDirectory(), "myDownload.txt");

        RequestBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("author", "coral_卡洛")
                .addFormDataPart("name", "myUpload", RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();

        final LoadRequest request = new LoadRequest.Builder()
                .url(requestUrl)
                .setMethod(HttpMethod.POST)
                .setRequestBody(multipartBody)
                .build();

        HttpManager.getInstance().post(request, new LoadCallback() {
            @Override
            public void onStart() {
                Log.e(TAG, "onStart");
            }

            @Override
            public void onProgress(long total, long current) {
                Log.e(TAG, "onProgress" + total + ", " + current);
            }

            @Override
            public void onSuccess(Object result) {
                Toast.makeText(FileLoadActivity.this, "upload success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code, String msg) {
                Log.e(TAG, "onFailed");
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "onFinish");
            }
        });

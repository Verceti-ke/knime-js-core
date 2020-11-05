module.exports = {
    chainWebpack: config => {
        config.optimization.splitChunks(false); // disabled to get one file containing the whole editor

        // enable vue-svg-loader
        const svgRule = config.module.rule('svg');
        svgRule.uses.clear();
        svgRule
            .use('vue-svg-loader')
            .loader('vue-svg-loader');
    },
    css: {
        extract: false // inline CSS in JS file for easy integration in KNIME AP
    },
    filenameHashing: false, // also disabled for KNIME AP integration
    productionSourceMap: false,
    publicPath: ''
};

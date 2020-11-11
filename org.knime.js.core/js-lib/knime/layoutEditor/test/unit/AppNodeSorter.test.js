import { shallowMount, createLocalVue } from '@vue/test-utils';
import AppNodeSorter from '~/src/components/AppNodeSorter';
import Vuex from 'vuex';
import * as workflowStoreConfig from '~/src/store/nodeSorter';
import Draggable from 'vuedraggable';
import KnimeView from '~/src/components/layout/KnimeView';

describe('AppNodeSorter.vue', () => {

    let localVue, store, mocks;

    beforeAll(() => {
        localVue = createLocalVue();
        localVue.use(Vuex);

        store = new Vuex.Store(workflowStoreConfig);

        store.state.layout = {
            rows: [{
                type: 'row',
                columns: [{
                    content: [{
                        nodeID: '1'
                    }]
                }]
            }, {
                type: 'row',
                columns: [{
                    content: [{
                        nodeID: '7'
                    }]
                }]
            }, {
                type: 'row',
                columns: [{
                    content: [{
                        nodeID: '2'
                    }]
                }]
            }]
        };

        mocks = { $store: store };
    });

    it('renders', () => {
        const wrapper = shallowMount(AppNodeSorter, {
            mocks
        });

        expect(wrapper.find(Draggable).exists()).toBe(true);

        let views = wrapper.findAll(KnimeView);
        let expectedViewCount = 3;
        expect(views.length).toBe(expectedViewCount);
        views.wrappers.forEach((view, index) => {
            expect(view.props('view')).toBe(store.state.layout.rows[index].columns[0].content[0]);
        });
    });

});

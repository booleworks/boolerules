import PrimeVue from 'primevue/config'
import FileUpload from 'primevue/fileupload';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import Tag from 'primevue/tag';
import Checkbox from 'primevue/checkbox';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Calendar from 'primevue/calendar';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import AutoComplete from 'primevue/autocomplete';
import Chips from 'primevue/chips';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Sidebar from 'primevue/sidebar';
import Tooltip from 'primevue/tooltip';
import Message from 'primevue/message';
import Chart from 'primevue/chart';

export default defineNuxtPlugin((nuxtApp) => {
    nuxtApp.vueApp.use(PrimeVue, { ripple: true });
    nuxtApp.vueApp.component('Button', Button)
    nuxtApp.vueApp.component('FileUpload', FileUpload)
    nuxtApp.vueApp.component('Divider', Divider)
    nuxtApp.vueApp.component('Tag', Tag)
    nuxtApp.vueApp.component('Checkbox', Checkbox)
    nuxtApp.vueApp.component('Dialog', Dialog)
    nuxtApp.vueApp.component('Dropdown', Dropdown)
    nuxtApp.vueApp.component('MultiSelect', MultiSelect)
    nuxtApp.vueApp.component('InputNumber', InputNumber)
    nuxtApp.vueApp.component('InputText', InputText)
    nuxtApp.vueApp.component('Calendar', Calendar)
    nuxtApp.vueApp.component('DataTable', DataTable)
    nuxtApp.vueApp.component('Column', Column)
    nuxtApp.vueApp.component('AutoComplete', AutoComplete)
    nuxtApp.vueApp.component('Chips', Chips)
    nuxtApp.vueApp.component('Accordion', Accordion)
    nuxtApp.vueApp.component('AccordionTab', AccordionTab)
    nuxtApp.vueApp.component('Sidebar', Sidebar)
    nuxtApp.vueApp.component('Message', Message)
    nuxtApp.vueApp.component('Chart', Chart)
    nuxtApp.vueApp.directive('Tooltip', Tooltip)
});
